package com.eshopper.catalog.shared.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("storage")
public record StorageProperties(
    String endpoint,
    String publicEndpoint,
    String bucket,
    String region,
    String accessKey,
    String secretKey,
    String publicBaseUrl,
    Duration uploadUrlTtl,
    long maxLogoBytes
) {}
