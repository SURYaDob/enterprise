package com.cdac.enterprise.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.UploadedDocumentResponse;
import com.cdac.enterprise.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin Documents", description = "Admin APIs for viewing uploaded document metadata")
@RestController
@RequestMapping(ApiPaths.ADMIN_APPLICATIONS)
public class DocumentAdminController {

    private final DocumentService documentService;

    public DocumentAdminController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(
            summary = "Get documents by application",
            description = "Returns uploaded document metadata for a specific application. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{applicationId}/documents")
    public ResponseEntity<ApiResponse<List<UploadedDocumentResponse>>> getDocumentsByApplication(
            @PathVariable Long applicationId
    ) {
        List<UploadedDocumentResponse> documents = documentService.getDocumentsByApplication(applicationId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.DOCUMENTS_FETCHED_SUCCESSFULLY,
                        documents
                ));
    }
}