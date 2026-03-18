package com.clara.ops.challenge.document_management_service_challenge.controller.model;

import lombok.Data;

import java.util.List;

@Data
public class DocumentSearchFilters {
    private String user;
    private String name;
    private List<String> tags;
}
