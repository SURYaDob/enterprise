package com.cdac.enterprise.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.AuditLogResponse;
import com.cdac.enterprise.service.AuditLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin Audit Logs", description = "Admin APIs for viewing system audit logs")
@RestController
@RequestMapping(ApiPaths.ADMIN_AUDIT_LOGS)
public class AuditLogAdminController {

    private final AuditLogService auditLogService;

    public AuditLogAdminController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(
            summary = "Get audit logs",
            description = "Returns paginated audit logs. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<AuditLogResponse> auditLogs = auditLogService.getAuditLogs(pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.AUDIT_LOGS_FETCHED_SUCCESSFULLY,
                        auditLogs
                ));
    }
}