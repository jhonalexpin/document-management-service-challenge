package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.controller.model.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.UploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.model.Document;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @Test
    void searchDocuments_success() {
        DocumentSearchFilters filters = new DocumentSearchFilters();
        filters.setUser("testUser");
        filters.setTags(Collections.singletonList("tag1"));

        PaginatedDocumentSearch paginatedResponse = new PaginatedDocumentSearch();
        paginatedResponse.setTotalElements(1L);
        paginatedResponse.setTotalPages(1);
        paginatedResponse.setSize(20);
        paginatedResponse.setNumber(0);
        paginatedResponse.setFirst(true);
        paginatedResponse.setLast(true);

        Document doc = new Document();
        doc.setId(1L);
        doc.setUserId("testUser");
        doc.setDocumentName("doc1");
        doc.setTags(Arrays.asList("tag1", "tag2"));
        doc.setFileType("application/pdf");
        doc.setFileSize(100L);
        doc.setMinioPath("testUser/doc1");
        doc.setCreatedAt(LocalDateTime.now());
        doc.setModifiedAt(LocalDateTime.now());

        DocumentResponse docResponse = new DocumentResponse();
        docResponse.setId(doc.getId());
        docResponse.setUser(doc.getUserId());
        docResponse.setName(doc.getDocumentName());
        docResponse.setTags(doc.getTags());
        docResponse.setFileType(doc.getFileType());
        docResponse.setFileSize(doc.getFileSize());
        docResponse.setMinioPath(doc.getMinioPath());
        docResponse.setCreatedAt(doc.getCreatedAt());
        docResponse.setModifiedAt(doc.getModifiedAt());

        paginatedResponse.setContent(Collections.singletonList(docResponse));

        when(documentDBService.getDocumentsByPaging(
                any(DocumentSearchFilters.class),
                any(String[].class),
                anyInt(),
                anyInt(),
                anyInt()
        )).thenReturn(Mono.just(paginatedResponse));

        StepVerifier.create(documentController.searchDocuments(filters, 0, 20))
                .expectNextMatches(response -> response.getTotalElements() == 1L &&
                        response.getContent().get(0).getName().equals("doc1"))
                .verifyComplete();
    }

    @Test
    void searchDocuments_noResults() {
        DocumentSearchFilters filters = new DocumentSearchFilters();
        filters.setUser("nonExistentUser");

        PaginatedDocumentSearch paginatedResponse = new PaginatedDocumentSearch();
        paginatedResponse.setTotalElements(0L);
        paginatedResponse.setTotalPages(0);
        paginatedResponse.setSize(20);
        paginatedResponse.setNumber(0);
        paginatedResponse.setFirst(true);
        paginatedResponse.setLast(true);
        paginatedResponse.setContent(Collections.emptyList());

        when(documentDBService.getDocumentsByPaging(
                any(DocumentSearchFilters.class),
                any(String[].class),
                anyInt(),
                anyInt(),
                anyInt()
        )).thenReturn(Mono.just(paginatedResponse));

        StepVerifier.create(documentController.searchDocuments(filters, 0, 20))
                .expectNextMatches(response -> response.getTotalElements() == 0L &&
                        response.getContent().isEmpty())
                .verifyComplete();
    }

    @Test
    void searchDocuments_serviceFails() {
        DocumentSearchFilters filters = new DocumentSearchFilters();
        filters.setUser("testUser");

        when(documentDBService.getDocumentsByPaging(
                any(DocumentSearchFilters.class),
                any(String[].class),
                anyInt(),
                anyInt(),
                anyInt()
        )).thenReturn(Mono.error(new RuntimeException("DB search failed")));

        StepVerifier.create(documentController.searchDocuments(filters, 0, 20))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("DB search failed"))
                .verify();
    }
}
