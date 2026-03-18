package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.controller.UploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentDBServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentDBService documentDBService;

    @Test
    void saveToDatabase_success() {
        UploadRequest request = new UploadRequest();
        request.setUser("testUser");
        request.setName("testFile");
        String minioPath = "testUser/testFile";
        FilePart filePart = mock(FilePart.class);
        HttpHeaders headers = mock(HttpHeaders.class);

        when(filePart.headers()).thenReturn(headers);
        when(headers.getContentLength()).thenReturn(100L);
        when(headers.getContentType()).thenReturn(MediaType.APPLICATION_PDF);

        Document savedDoc = new Document();
        savedDoc.setId(1L);
        savedDoc.setUserId("testUser");
        savedDoc.setDocumentName("testFile");
        savedDoc.setMinioPath(minioPath);
        savedDoc.setFileSize(100L);
        savedDoc.setFileType("application/pdf");
        savedDoc.setCreatedAt(LocalDateTime.now());
        savedDoc.setModifiedAt(LocalDateTime.now());

        when(documentRepository.save(any(Document.class))).thenReturn(Mono.just(savedDoc));

        StepVerifier.create(documentDBService.saveToDatabase(request, minioPath, filePart))
                .expectNext(savedDoc)
                .verifyComplete();
    }

    @Test
    void saveToDatabase_fails() {
        UploadRequest request = new UploadRequest();
        request.setUser("testUser");
        request.setName("testFile");
        String minioPath = "testUser/testFile";
        FilePart filePart = mock(FilePart.class);
        HttpHeaders headers = mock(HttpHeaders.class);

        when(filePart.headers()).thenReturn(headers);
        when(headers.getContentLength()).thenReturn(100L);
        when(headers.getContentType()).thenReturn(MediaType.APPLICATION_PDF);

        when(documentRepository.save(any(Document.class))).thenReturn(Mono.error(new RuntimeException("DB Save Error")));

        StepVerifier.create(documentDBService.saveToDatabase(request, minioPath, filePart))
                .expectErrorMessage("DB Save Error")
                .verify();
    }
}
