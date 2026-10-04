package com.eshopper.catalog.brand.dto;

import java.util.UUID;

public record BrandResponse(UUID id, String slug, String name, String logoUrl, Long version) {
}
