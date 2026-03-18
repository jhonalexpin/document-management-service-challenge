package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.controller.UploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class DocumentDBService {

    private DocumentRepository documentRepository;

    public Mono<Document> saveToDatabase(UploadRequest request, String minioPath, FilePart file) {
        Document doc = new Document();
        doc.setUserId(request.getUser());
        doc.setDocumentName(request.getName());
        doc.setTags(request.getTags());
        doc.setMinioPath(minioPath);
        doc.setFileSize(file.headers().getContentLength());
        doc.setFileType(file.headers().getContentType().toString());
        doc.setCreatedAt(LocalDateTime.now());
        doc.setModifiedAt(LocalDateTime.now());

        return documentRepository.save(doc);
    }
}
