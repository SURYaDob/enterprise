package com.cdac.enterprise.dto;

import java.time.LocalDateTime;

import com.cdac.enterprise.constant.DocumentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedDocumentResponse {

    private Long id;

    private Long applicationId;
    private Long userId;

    private DocumentType documentType;
    private String originalFileName;
    private String storedFileName;
    private String contentType;
    private Long fileSize;

    private LocalDateTime uploadedAt;
}