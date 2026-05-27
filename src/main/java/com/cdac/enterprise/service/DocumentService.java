package com.cdac.enterprise.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.cdac.enterprise.constant.DocumentType;
import com.cdac.enterprise.dto.UploadedDocumentResponse;

public interface DocumentService {

    UploadedDocumentResponse uploadDocument(
            String studentEmail,
            String ipAddress,
            Long applicationId,
            DocumentType documentType,
            MultipartFile file
    );

    List<UploadedDocumentResponse> getMyDocuments(String studentEmail);

    List<UploadedDocumentResponse> getDocumentsByApplication(Long applicationId);
}