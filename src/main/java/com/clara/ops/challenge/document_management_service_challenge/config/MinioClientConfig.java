package com.clara.ops.challenge.document_management_service_challenge.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@AllArgsConstructor
public class MinioClientConfig {

    private final MinioProperties minioProperties;

    @Bean
    public S3AsyncClient s3AsyncClient() {

        return S3AsyncClient.crtBuilder()
                .endpointOverride(URI.create(minioProperties.getEndpoint())) // MinIO URL
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(minioProperties.getAccessKey(), minioProperties.getSecretKey())))
                .forcePathStyle(true) // Required for MinIO
                .targetThroughputInGbps(1.0) // Adjust based on your network
                .minimumPartSizeInBytes(8 * 1024 * 1024L) // 8MB chunks
                .build();
    }

}
