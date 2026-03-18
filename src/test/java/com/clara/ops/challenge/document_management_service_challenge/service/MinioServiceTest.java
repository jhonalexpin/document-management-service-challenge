package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private MinioService minioService;

    @Test
    void uploadDocumentToMinio_success() throws Exception {
        String user = "testUser";
        String name = "testFile.pdf";
        String expectedKey = user + "/" + name;
        String content = "dummy content";

        FilePart filePart = mock(FilePart.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(filePart.headers()).thenReturn(headers);
        when(headers.getContentType()).thenReturn(MediaType.APPLICATION_PDF);

        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(content.getBytes(StandardCharsets.UTF_8));
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));

        when(minioProperties.getBucketName()).thenReturn("test-bucket");

        // Mock putObject to do nothing (success)
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        StepVerifier.create(minioService.uploadDocumentToMinio(filePart, user, name))
                .expectNext(expectedKey)
                .verifyComplete();

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadDocumentToMinio_failure() throws Exception {
        String user = "testUser";
        String name = "testFile.pdf";
        String content = "dummy content";

        FilePart filePart = mock(FilePart.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(filePart.headers()).thenReturn(headers);
        when(headers.getContentType()).thenReturn(MediaType.APPLICATION_PDF);

        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(content.getBytes(StandardCharsets.UTF_8));
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));

        when(minioProperties.getBucketName()).thenReturn("test-bucket");

        // Mock putObject to throw exception
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("MinIO error"));

        StepVerifier.create(minioService.uploadDocumentToMinio(filePart, user, name))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("MinIO Upload Failed"))
                .verify();

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void generatePresignedUrl_success() throws Exception {
        String minioPath = "testUser/doc1.pdf";
        String expectedUrl = "http://minio/presigned-url";
        String bucketName = "test-bucket";

        when(minioProperties.getBucketName()).thenReturn(bucketName);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(expectedUrl);

        StepVerifier.create(minioService.generatePresignedUrl(minioPath))
                .expectNext(expectedUrl)
                .verifyComplete();

        ArgumentCaptor<GetPresignedObjectUrlArgs> captor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(minioClient).getPresignedObjectUrl(captor.capture());
        GetPresignedObjectUrlArgs capturedArgs = captor.getValue();
        assertEquals(bucketName, capturedArgs.bucket());
        assertEquals(minioPath, capturedArgs.object());
        assertEquals(Method.GET, capturedArgs.method());
    }

    @Test
    void generatePresignedUrl_failure() throws Exception {
        String minioPath = "testUser/doc1.pdf";
        String bucketName = "test-bucket";

        when(minioProperties.getBucketName()).thenReturn(bucketName);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenThrow(new RuntimeException("MinIO error"));

        StepVerifier.create(minioService.generatePresignedUrl(minioPath))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("MinIO error"))
                .verify();
    }
}
