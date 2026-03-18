package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.service.DocumentDBService;
import com.clara.ops.challenge.document_management_service_challenge.service.MinioService;
import com.clara.ops.challenge.document_management_service_challenge.utils.FileValidatorUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private MinioService minioService;

    @Mock
    private DocumentDBService documentDBService;

    @Mock
    private FileValidatorUtils fileValidator;

    @InjectMocks
    private DocumentController documentController;

    @Mock
    private FilePart filePart;

    @Test
    void uploadDocument_success() {
        UploadRequest request = new UploadRequest();
        request.setUser("testUser");
        request.setName("testFile");

        when(fileValidator.isSupported(any(FilePart.class))).thenReturn(true);
        when(minioService.uploadDocumentToMinio(any(FilePart.class), any(String.class), any(String.class)))
                .thenReturn(Mono.just("s3Key"));
        com.clara.ops.challenge.document_management_service_challenge.model.Document savedDoc = new com.clara.ops.challenge.document_management_service_challenge.model.Document();
        savedDoc.setId(1L);
        when(documentDBService.saveToDatabase(any(UploadRequest.class), any(String.class), any(FilePart.class)))
                .thenReturn(Mono.just(savedDoc));

        StepVerifier.create(documentController.uploadDocument(request, filePart))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.CREATED &&
                        Objects.equals(response.getBody(), "Document uploaded successfully with ID: 1"))
                .verifyComplete();
    }

    @Test
    void uploadDocument_unsupportedFileType() {
        UploadRequest request = new UploadRequest();
        request.setUser("testUser");
        request.setName("testFile");

        when(fileValidator.isSupported(any(FilePart.class))).thenReturn(false);

        StepVerifier.create(documentController.uploadDocument(request, filePart))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.UNSUPPORTED_MEDIA_TYPE &&
                        Objects.equals(response.getBody(), "Only PDF files are allowed."))
                .verifyComplete();
    }

    @Test
    void uploadDocument_minioFails() {
        UploadRequest request = new UploadRequest();
        request.setUser("testUser");
        request.setName("testFile");

        when(fileValidator.isSupported(any(FilePart.class))).thenReturn(true);
        when(minioService.uploadDocumentToMinio(any(FilePart.class), any(String.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("MinIO error")));

        StepVerifier.create(documentController.uploadDocument(request, filePart))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                        Objects.equals(response.getBody(), "Failed to upload file: MinIO error"))
                .verifyComplete();
    }

    @Test
    void uploadDocument_databaseFails() {
        UploadRequest request = new UploadRequest();
        request.setUser("testUser");
        request.setName("testFile");

        when(fileValidator.isSupported(any(FilePart.class))).thenReturn(true);
        when(minioService.uploadDocumentToMinio(any(FilePart.class), any(String.class), any(String.class)))
                .thenReturn(Mono.just("s3Key"));
        when(documentDBService.saveToDatabase(any(UploadRequest.class), any(String.class), any(FilePart.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(documentController.uploadDocument(request, filePart))
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                        Objects.equals(response.getBody(), "Failed to upload file: DB error"))
                .verifyComplete();
    }
}
