package com.eshopper.catalog.brand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBrandRequest(
        @NotBlank @Size(max = 100) String name,
        String logoUploadKey
) {}
