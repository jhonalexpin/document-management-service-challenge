package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.controller.model.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.UploadRequest;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Test
    void getDocumentsByPaging_success() {
        DocumentSearchFilters filters = new DocumentSearchFilters();
        filters.setUser("testUser");
        filters.setName("testFile");

        String[] tagsArray = {"tag1", "tag2"};
        int offset = 0;
        int page = 0;
        int size = 20;

        Document doc1 = new Document();
        doc1.setId(1L);
        doc1.setUserId("testUser");
        doc1.setDocumentName("testFile1");
        doc1.setTags(Arrays.asList("tag1", "tag2"));

        Document doc2 = new Document();
        doc2.setId(2L);
        doc2.setUserId("testUser");
        doc2.setDocumentName("testFile2");
        doc2.setTags(Arrays.asList("tag1", "tag3"));

        List<Document> documentList = Arrays.asList(doc1, doc2);

        when(documentRepository.findByFilters(
                anyString(),
                anyString(),
                any(String[].class),
                anyInt(),
                anyInt()
        )).thenReturn(Flux.fromIterable(documentList));

        when(documentRepository.countByFilters(
                anyString(),
                anyString(),
                any(String[].class)
        )).thenReturn(Mono.just(2L));

        StepVerifier.create(documentDBService.getDocumentsByPaging(filters, tagsArray, offset, page, size))
                .expectNextMatches(paginatedResponse -> paginatedResponse.getTotalElements() == 2L &&
                        paginatedResponse.getContent().size() == 2 &&
                        paginatedResponse.getContent().get(0).getName().equals("testFile1") &&
                        paginatedResponse.getContent().get(1).getName().equals("testFile2"))
                .verifyComplete();
    }

    @Test
    void getDocumentsByPaging_emptyResults() {
        DocumentSearchFilters filters = new DocumentSearchFilters();
        filters.setUser("nonExistentUser");
        filters.setName("nonExistentFile");

        String[] tagsArray = {"tag1", "tag2"};
        int offset = 0;
        int page = 0;
        int size = 20;

        when(documentRepository.findByFilters(
                anyString(),
                anyString(),
                any(String[].class),
                anyInt(),
                anyInt()
        )).thenReturn(Flux.empty());

        when(documentRepository.countByFilters(
                anyString(),
                anyString(),
                any(String[].class)
        )).thenReturn(Mono.just(0L));

        StepVerifier.create(documentDBService.getDocumentsByPaging(filters, tagsArray, offset, page, size))
                .expectNextMatches(paginatedResponse -> paginatedResponse.getTotalElements() == 0L &&
                        paginatedResponse.getContent().isEmpty())
                .verifyComplete();
    }

    @Test
    void getDocumentsByPaging_repositoryFails() {
        DocumentSearchFilters filters = new DocumentSearchFilters();
        filters.setUser("testUser");
        filters.setName("testFile");

        String[] tagsArray = {"tag1", "tag2"};
        int offset = 0;
        int page = 0;
        int size = 20;

        when(documentRepository.findByFilters(
                anyString(),
                anyString(),
                any(String[].class),
                anyInt(),
                anyInt()
        )).thenReturn(Flux.error(new RuntimeException("DB findByFilters failed")));

        when(documentRepository.countByFilters(
                anyString(),
                anyString(),
                any(String[].class)
        )).thenReturn(Mono.just(0L));

        StepVerifier.create(documentDBService.getDocumentsByPaging(filters, tagsArray, offset, page, size))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("DB findByFilters failed"))
                .verify();
    }
}
