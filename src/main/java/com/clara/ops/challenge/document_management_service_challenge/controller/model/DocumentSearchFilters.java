package com.clara.ops.challenge.document_management_service_challenge.controller.model;

import lombok.Data;

import java.util.List;

/**
 * Represents the search filters used to query documents.
 */
@Data
public class DocumentSearchFilters {
    /**
     * Filter by user ID.
     */
    private String user;
    /**
     * Filter by document name (partial match).
     */
    private String name;
    /**
     * Filter by tags (documents containing all specified tags).
     */
    private List<String> tags;
}
