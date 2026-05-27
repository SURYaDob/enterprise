package com.cdac.enterprise.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.constant.DocumentType;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.UploadedDocumentResponse;
import com.cdac.enterprise.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "Documents", description = "Student APIs for uploading and viewing documents")
@RestController
@RequestMapping(ApiPaths.DOCUMENTS)
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(
            summary = "Upload document",
            description = "Allows a student to upload a document for their own application"
    )
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadedDocumentResponse>> uploadDocument(
            Principal principal,
            HttpServletRequest servletRequest,
            @RequestParam Long applicationId,
            @RequestParam DocumentType documentType,
            @RequestParam MultipartFile file
    ) {
        UploadedDocumentResponse uploadedDocumentResponse = documentService.uploadDocument(
                principal.getName(),
                getClientIp(servletRequest),
                applicationId,
                documentType,
                file
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        AppMessages.DOCUMENT_UPLOADED_SUCCESSFULLY,
                        uploadedDocumentResponse
                ));
    }

    @Operation(
            summary = "Get my documents",
            description = "Returns documents uploaded by the logged-in student"
    )
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UploadedDocumentResponse>>> getMyDocuments(
            Principal principal
    ) {
        List<UploadedDocumentResponse> documents = documentService.getMyDocuments(principal.getName());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.DOCUMENTS_FETCHED_SUCCESSFULLY,
                        documents
                ));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}