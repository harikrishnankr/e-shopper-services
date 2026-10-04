package com.eshopper.catalog.shared.storage;

import java.time.Instant;

public record PresignedUpload(String key, String uploadUrl, Instant expiresAt) {}
