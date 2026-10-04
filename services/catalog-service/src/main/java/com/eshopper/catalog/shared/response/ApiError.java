package com.eshopper.catalog.shared.response;

import java.util.List;

public record ApiError(String code, String message, List<FieldViolation> details) {

    public ApiError(String code, String message) {
        this(code, message, null);
    }
}
