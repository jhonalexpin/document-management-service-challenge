package com.clara.ops.challenge.document_management_service_challenge.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("documents")
public class Document {

    @Id
    private Long id;

    private String userId;

    private String documentName;

    private List<String> tags;

    private String minioPath;

    private Long fileSize;

    private String fileType;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;
}
