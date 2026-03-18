package com.clara.ops.challenge.document_management_service_challenge.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a document entity stored in the system.
 * This model maps to the "documents" table in the database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("documents")
public class Document {

    /**
     * The unique identifier for the document.
     */
    @Id
    private Long id;

    /**
     * The ID of the user who owns the document.
     */
    private String userId;

    /**
     * The name of the document.
     */
    private String documentName;

    /**
     * A list of tags associated with the document for categorization and searching.
     */
    private List<String> tags;

    /**
     * The path to the document in MinIO storage.
     */
    private String minioPath;

    /**
     * The size of the document file in bytes.
     */
    private Long fileSize;

    /**
     * The MIME type of the document file.
     */
    private String fileType;

    /**
     * The timestamp when the document was created.
     */
    private LocalDateTime createdAt;

    /**
     * The timestamp when the document was last modified.
     */
    private LocalDateTime modifiedAt;
}
