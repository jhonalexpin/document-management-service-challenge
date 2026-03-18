package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.controller.model.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.UploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for handling database operations related to documents.
 */
@Service
@AllArgsConstructor
public class DocumentDBService {

    private DocumentRepository documentRepository;

    /**
     * Saves document metadata to the database.
     *
     * @param request   The upload request containing metadata like user, name, and tags.
     * @param minioPath The path of the document in MinIO storage.
     * @param file      The file part containing the uploaded file's details.
     * @return A Mono emitting the saved Document entity.
     */
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

    /**
     * Retrieves a paginated list of documents based on search filters.
     *
     * @param filters   The search criteria (user, name, tags).
     * @param tagsArray An array of tags for filtering.
     * @param offset    The starting offset for pagination.
     * @param page      The current page number.
     * @param size      The number of items per page.
     * @return A Mono emitting a PaginatedDocumentSearch object with the search results.
     */
    public Mono<PaginatedDocumentSearch> getDocumentsByPaging(DocumentSearchFilters filters, String[] tagsArray, int offset, int page, int size) {
        Mono<List<Document>> contentMono = documentRepository.findByFilters(filters.getUser(), filters.getName(), tagsArray, size, offset).collectList();
        Mono<Long> totalMono = documentRepository.countByFilters(filters.getUser(), filters.getName(), tagsArray);

        return Mono.zip(contentMono, totalMono).map(tuple -> {
            List<Document> content = tuple.getT1();
            long total = tuple.getT2();
            PaginatedDocumentSearch response = new PaginatedDocumentSearch();
            response.setContent(content.stream().map(this::toDocumentResponse).toList());
            response.setTotalPages((int) Math.ceil((double) total / size));
            response.setTotalElements(total);
            response.setSize(size);
            response.setNumber(page);
            response.setFirst(page == 0);
            response.setLast((page + 1) * size >= total);
            return response;
        });
    }

    /**
     * Converts a Document entity to a DocumentResponse DTO.
     *
     * @param doc The Document entity to convert.
     * @return The corresponding DocumentResponse DTO.
     */
    private DocumentResponse toDocumentResponse(Document doc) {
        DocumentResponse resp = new DocumentResponse();
        resp.setId(doc.getId());
        resp.setUser(doc.getUserId());
        resp.setName(doc.getDocumentName());
        resp.setTags(doc.getTags());
        resp.setMinioPath(doc.getMinioPath());
        resp.setFileSize(doc.getFileSize());
        resp.setFileType(doc.getFileType());
        resp.setCreatedAt(doc.getCreatedAt());
        resp.setModifiedAt(doc.getModifiedAt());
        return resp;
    }
}
