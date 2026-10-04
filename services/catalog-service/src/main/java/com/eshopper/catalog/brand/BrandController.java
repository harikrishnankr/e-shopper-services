package com.eshopper.catalog.brand;

import com.eshopper.catalog.brand.dto.BrandResponse;
import com.eshopper.catalog.brand.dto.CreateBrandRequest;
import com.eshopper.catalog.brand.dto.LogoUploadRequest;
import com.eshopper.catalog.brand.dto.UpdateBrandRequest;
import com.eshopper.catalog.shared.storage.PresignedUpload;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/brand")
public class BrandController {
    private final BrandService service;

    public BrandController(BrandService service) {
        this.service = service;
    }

    @PostMapping("/logo-uploads")
    PresignedUpload createLogoUpload(@Valid @RequestBody LogoUploadRequest req) {
        return service.createLogoUpload(req.contentType());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    BrandResponse create(@Valid @RequestBody CreateBrandRequest req) {
        return service.create(req);
    }

    @GetMapping("/{slug}")
    BrandResponse get(@PathVariable String slug) {
        return service.getBySlug(slug);
    }

    @GetMapping("/list")
    List<BrandResponse> getAll() {
        return service.getAllBrands();
    }

    @DeleteMapping("/{id}")
    boolean delete(@PathVariable UUID id) {
        service.delete(id);
        return true;
    }

    @PatchMapping("/{id}")
    BrandResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateBrandRequest req) {
        return service.update(id, req);
    }
}
