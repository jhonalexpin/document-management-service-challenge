package com.clara.ops.challenge.document_management_service_challenge.controller.model;

import lombok.Data;

import java.util.List;

@Data
public class UploadRequest {
    private String user;
    private String name;
    private List<String> tags;
}
