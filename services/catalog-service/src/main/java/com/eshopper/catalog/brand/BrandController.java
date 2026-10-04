package com.eshopper.catalog.brand;

import com.eshopper.catalog.brand.dto.BrandResponse;
import com.eshopper.catalog.brand.dto.CreateBrandRequest;
import com.eshopper.catalog.brand.dto.LogoUploadRequest;
import com.eshopper.catalog.brand.dto.UpdateBrandRequest;
import com.eshopper.catalog.shared.response.ApiResponse;
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
    ApiResponse<PresignedUpload> createLogoUpload(@Valid @RequestBody LogoUploadRequest req) {
        return ApiResponse.success(service.createLogoUpload(req.contentType()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<BrandResponse> create(@Valid @RequestBody CreateBrandRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @GetMapping("/{slug}")
    ApiResponse<BrandResponse> get(@PathVariable String slug) {
        return ApiResponse.success(service.getBySlug(slug));
    }

    @GetMapping("/list")
    ApiResponse<List<BrandResponse>> getAll() {
        return ApiResponse.success(service.getAllBrands());
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}")
    ApiResponse<BrandResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateBrandRequest req) {
        return ApiResponse.success(service.update(id, req));
    }
}
