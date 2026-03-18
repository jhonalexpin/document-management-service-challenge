package com.clara.ops.challenge.document_management_service_challenge.controller;


import com.clara.ops.challenge.document_management_service_challenge.controller.model.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.UploadRequest;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentDBService;
import com.clara.ops.challenge.document_management_service_challenge.service.MinioService;
import com.clara.ops.challenge.document_management_service_challenge.utils.FileValidatorUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/document-management")
@AllArgsConstructor
@Slf4j
public class DocumentController {

    private final FileValidatorUtils fileValidator;
    private final MinioService minioService;
    private final DocumentDBService documentDBService;



    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> uploadDocument(@ModelAttribute UploadRequest request, @RequestPart("file") FilePart filePart) {

        // 1. Immediate Validation
        if (!fileValidator.isSupported(filePart)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body("Only PDF files are allowed."));
        }

        return minioService.uploadDocumentToMinio(filePart, request.getUser(), request.getName())
                .flatMap(s3Key -> documentDBService.saveToDatabase(request, s3Key, filePart))
                .map(savedDoc -> ResponseEntity.status(HttpStatus.CREATED).body("Document uploaded successfully with ID: " + savedDoc.getId()))
                .onErrorResume(e -> {
                    log.error("Error during file upload: ", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to upload file: " + e.getMessage()));
                });
    }

    @PostMapping("/search")
    public Mono<PaginatedDocumentSearch> searchDocuments(@RequestBody DocumentSearchFilters filters,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        String[] tagsArray = filters.getTags() != null ? filters.getTags().stream().map(String::toLowerCase)
                .toArray(String[]::new) : null;
        int offset = page * size;

        return documentDBService.getDocumentsByPaging(filters, tagsArray, offset, page, size);
    }


}
