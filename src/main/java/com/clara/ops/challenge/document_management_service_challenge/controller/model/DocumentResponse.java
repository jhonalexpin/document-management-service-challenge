package com.clara.ops.challenge.document_management_service_challenge.controller.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) representing the response for a document search or retrieval operation.
 */
@Data
public class DocumentResponse {
    /**
     * The unique identifier of the document.
     */
    private Long id;
    /**
     * The user ID of the document owner.
     */
    private String user;
    /**
     * The name of the document.
     */
    private String name;
    /**
     * The list of tags associated with the document.
     */
    private List<String> tags;
    /**
     * The path of the document in MinIO storage.
     */
    private String minioPath;
    /**
     * The size of the document file.
     */
    private Long fileSize;
    /**
     * The type of the document file.
     */
    private String fileType;
    /**
     * The creation timestamp of the document.
     */
    private LocalDateTime createdAt;
    /**
     * The last modification timestamp of the document.
     */
    private LocalDateTime modifiedAt;
}
