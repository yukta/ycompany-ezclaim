package com.ycompany.claimservice.service;

import com.ycompany.claimservice.dto.PresignResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class DocumentService {

    private final S3Presigner presigner;

    @Value("${documents.bucket}")
    private String bucket;

    public DocumentService(@Value("${aws.endpoint}") String endpoint,
                           @Value("${aws.region}") String region) {

        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build())
                .build();

    }

    public PresignResponse presignUpload(String claimId,
                                         String fileName,
                                         String contentType) {

        String objectKey =
                "claims/" + claimId + "/documents/" + fileName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(putRequest)
                        .build();

        PresignedPutObjectRequest presigned =
                presigner.presignPutObject(presignRequest);

        return new PresignResponse(
                presigned.url().toString(),
                objectKey);
    }

    public Map<String, String> confirmUpload(String claimId,
                                             Map<String, String> payload) {

        String objectKey = payload.get("objectKey");

        if (objectKey == null || objectKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "objectKey is required"
            );
        }

        return Map.of(
                "claimId", claimId,
                "objectKey", objectKey,
                "status", "CONFIRMED",
                "confirmedAt", Instant.now().toString()
        );
    }
}
