package com.eshopper.catalog.shared.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class StorageConfig {

    private StaticCredentialsProvider credentials(StorageProperties p) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(p.accessKey(), p.secretKey()));
    }

    @Bean
    public S3Client s3Client(StorageProperties p) {
        return S3Client.builder()
                .endpointOverride(URI.create(p.endpoint()))
                .region(Region.of(p.region()))
                .credentialsProvider(credentials(p))
                .serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build()
                ).build();
    }

    @Bean
    S3Presigner s3Presigner(StorageProperties p) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(p.publicEndpoint()))   // host the browser can reach
                .region(Region.of(p.region()))
                .credentialsProvider(credentials(p))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
