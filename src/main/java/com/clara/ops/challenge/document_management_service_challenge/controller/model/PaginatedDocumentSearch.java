package com.clara.ops.challenge.document_management_service_challenge.controller.model;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedDocumentSearch {
    private List<DocumentResponse> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
    private boolean first;
    private boolean last;
}
