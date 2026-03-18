package com.clara.ops.challenge.document_management_service_challenge.controller.model;

import lombok.Data;

import java.util.List;

/**
 * Represents the request body for uploading a document.
 */
@Data
public class UploadRequest {
    /**
     * The user ID of the document owner.
     */
    private String user;
    /**
     * The name of the document.
     */
    private String name;
    /**
     * A list of tags to associate with the document.
     */
    private List<String> tags;
}
