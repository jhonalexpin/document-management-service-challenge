package com.clara.ops.challenge.document_management_service_challenge.config;

import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class MinioClientConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        // Manually setting a small connection pool for the underlying HTTP client
        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder()
                .connectionPool(new okhttp3.ConnectionPool(3, 5, java.util.concurrent.TimeUnit.MINUTES))
                .build();

        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .httpClient(httpClient)
                .build();
    }

}
