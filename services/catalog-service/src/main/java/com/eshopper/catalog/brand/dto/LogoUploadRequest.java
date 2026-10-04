package com.eshopper.catalog.brand.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoUploadRequest(@NotBlank String contentType) {}
