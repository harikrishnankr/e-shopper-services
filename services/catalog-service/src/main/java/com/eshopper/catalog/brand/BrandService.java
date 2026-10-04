package com.eshopper.catalog.brand;

import com.eshopper.catalog.brand.dto.BrandResponse;
import com.eshopper.catalog.brand.dto.CreateBrandRequest;
import com.eshopper.catalog.brand.dto.UpdateBrandRequest;
import com.eshopper.catalog.shared.exception.BadRequestException;
import com.eshopper.catalog.shared.exception.ConflictException;
import com.eshopper.catalog.shared.exception.NotFoundException;
import com.eshopper.catalog.shared.storage.ObjectStorage;
import com.eshopper.catalog.shared.storage.PresignedUpload;
import com.eshopper.catalog.shared.storage.StorageProperties;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class BrandService {
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/png", "png",
            "image/webp", "webp",
            "image/jpeg", "jpg");
    private static final String TMP_PREFIX = "tmp/brand-logos/";
    private static final Pattern TMP_KEY = Pattern.compile(
            "^tmp/brand-logos/[0-9a-f-]{36}\\.(png|webp|jpg)$");

    private final BrandRepository repo;
    private final ObjectStorage storage;
    private final StorageProperties props;

    public BrandService(BrandRepository repo, ObjectStorage storage, StorageProperties p) {
        this.repo = repo;
        this.storage = storage;
        this.props = p;
    }

    public PresignedUpload createLogoUpload(String contentType) {
        var ext = ALLOWED_TYPES.get(contentType);
        if (ext == null) {
            throw new BadRequestException("Unsupported image type: " + contentType);
        }

        return storage.presignPut(TMP_PREFIX + UUID.randomUUID() + "." + ext, contentType);
    }

    public BrandResponse create(CreateBrandRequest req) {
        var name = req.name();
        var slug = Slugs.from(name);

        if (name.isEmpty()) {
            throw new BadRequestException("Brand name must contain letters or digits");
        }

        if (repo.existsBySlug(slug)) {
            throw new ConflictException("Brand already exists: " + name);
        }

        Brand brand = new Brand(name, slug);
        if (req.logoUploadKey() != null) {
            brand.changeLogo(promoteLogo(req.logoUploadKey(), brand.getId()));
        }

        repo.save(brand);
        return toResponse(brand);
    }

    public BrandResponse getBySlug(String slug) {
        Optional<Brand> brand = repo.findBySlug(slug);

        return toResponse(brand.orElseThrow(() -> new NotFoundException("Brand doesn't exists")));
    }

    public List<BrandResponse> getAllBrands() {
        List<Brand> brandList = repo.findAll();

        return brandList.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    public BrandResponse update(UUID id, UpdateBrandRequest req) {
        if (req.logoUploadKey() != null && req.shouldRemoveLogo()) {
            throw new BadRequestException("Use either logoUploadKey or removeLogo, not both");
        }

        var brand = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Brand not found: " + id));

        if (!brand.getVersion().equals(req.version())) {
            throw new ConflictException("Brand was modified by someone else; reload and retry");
        }

        if (req.name() != null) {
            var name = req.name().strip();
            var slug = Slugs.from(name);
            if (slug.isEmpty()) {
                throw new BadRequestException("Brand name must contain letters or digits");
            }
            if (repo.existsBySlugAndIdNot(slug, id)) {
                throw new ConflictException("Brand already exists: " + slug);
            }
            brand.rename(name, slug);
        }

        var oldLogoKey = brand.getLogoKey();
        String newLogoKey = null;

        if (req.logoUploadKey() != null) {
            newLogoKey = promoteLogo(req.logoUploadKey(), id);
            brand.changeLogo(newLogoKey);
        } else if (req.shouldRemoveLogo()) {
            brand.removeLogo();
        }

        Brand saved;
        try {
            saved = repo.save(brand);   // merge; @Version also catches races after the check above
        } catch (RuntimeException e) {
            if (newLogoKey != null) storage.delete(newLogoKey);   // undo the move
            throw e;
        }

        boolean logoReplacedOrRemoved = oldLogoKey != null && !oldLogoKey.equals(saved.getLogoKey());
        if (logoReplacedOrRemoved) {
            storage.delete(oldLogoKey);   // only after the DB no longer points at it
        }

        return toResponse(saved);
    }

    private String promoteLogo(String tmpKey, UUID brandId) {
        if (!TMP_KEY.matcher(tmpKey).matches()) {
            throw new BadRequestException("Invalid upload key");
        }

        long size = storage.sizeOf(tmpKey)
                .orElseThrow(() -> new BadRequestException("Logo was not uploaded"));
        if (size > props.maxLogoBytes()) {
            storage.delete(tmpKey);
            throw new BadRequestException("Logo exceeds 2 MB");
        }

        var ext = tmpKey.substring(tmpKey.lastIndexOf('.'));
        var finalKey = "brand-logos/" + brandId + "/" + UUID.randomUUID() + ext;
        storage.move(tmpKey, finalKey);
        return finalKey;
    }

    private BrandResponse toResponse(Brand b) {
        return new BrandResponse(b.getId(), b.getSlug(), b.getName(),
                storage.publicUrl(b.getLogoKey()), b.getVersion());
    }
}
