package com.clara.ops.challenge.document_management_service_challenge.controller.model;

import lombok.Data;

import java.util.List;

/**
 * Represents a paginated search result for documents.
 */
@Data
public class PaginatedDocumentSearch {
    /**
     * The list of {@link DocumentResponse} objects for the current page.
     */
    private List<DocumentResponse> content;
    /**
     * The total number of pages available.
     */
    private int totalPages;
    /**
     * The total number of elements across all pages.
     */
    private long totalElements;
    /**
     * The maximum number of elements per page.
     */
    private int size;
    /**
     * The current page number (0-indexed).
     */
    private int number;
    /**
     * Indicates if the current page is the first page.
     */
    private boolean first;
    /**
     * Indicates if the current page is the last page.
     */
    private boolean last;
}
