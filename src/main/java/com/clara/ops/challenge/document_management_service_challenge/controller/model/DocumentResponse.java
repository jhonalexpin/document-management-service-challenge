package com.clara.ops.challenge.document_management_service_challenge.controller.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocumentResponse {
    private Long id;
    private String user;
    private String name;
    private List<String> tags;
    private String minioPath;
    private Long fileSize;
    private String fileType;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
