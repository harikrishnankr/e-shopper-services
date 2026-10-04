package com.eshopper.catalog.brand.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateBrandRequest(
        @Size(max = 100) @Pattern(regexp = ".*\\S.*", message = "must not be blank")
        String name,                 // null = unchanged
        String logoUploadKey,        // null = unchanged
        Boolean removeLogo,
        @NotNull Long version
) {
    public boolean shouldRemoveLogo() {
        return Boolean.TRUE.equals(removeLogo);
    }
}