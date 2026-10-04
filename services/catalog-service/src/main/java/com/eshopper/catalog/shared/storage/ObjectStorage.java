package com.eshopper.catalog.shared.storage;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Instant;
import java.util.Optional;

@Component
public class ObjectStorage {
    private final S3Client s3;
    private final S3Presigner presigner;
    private final StorageProperties properties;

    public ObjectStorage(S3Client s3, S3Presigner presigner, StorageProperties p) {
        this.s3 = s3;
        this.presigner = presigner;
        this.properties = p;
    }

    public PresignedUpload presignPut(String key, String contentType) {
        var put = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .build();

        var signed = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                    .signatureDuration(properties.uploadUrlTtl())
                    .putObjectRequest(put)
                    .build()
        );

        return new PresignedUpload(
                key,
                signed.url().toString(),
                Instant.now().plus(properties.uploadUrlTtl())
        );
    }

    public Optional<Long> sizeOf(String key) {
        try {
            return Optional.of(s3.headObject(b -> b.bucket(properties.bucket()).key(key)).contentLength());
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    public void delete(String key) {
        s3.deleteObject(b -> b.bucket(properties.bucket()).key(key));
    }

    public void move(String fromKey, String toKey) {
        s3.copyObject(b -> b
                .sourceBucket(properties.bucket()).sourceKey(fromKey)
                .destinationBucket(properties.bucket()).destinationKey(toKey));
        delete(fromKey);
    }

    public String publicUrl(String key) {
        return key == null ? null : properties.publicBaseUrl() + "/" + key;
    }

}
