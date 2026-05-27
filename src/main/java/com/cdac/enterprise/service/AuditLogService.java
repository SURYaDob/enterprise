package com.cdac.enterprise.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cdac.enterprise.constant.AuditAction;
import com.cdac.enterprise.dto.AuditLogResponse;
import com.cdac.enterprise.entity.User;

public interface AuditLogService {

    void logAction(
            User actorUser,
            String actorEmail,
            AuditAction action,
            String entityType,
            Long entityId,
            String description,
            String ipAddress
    );

    Page<AuditLogResponse> getAuditLogs(Pageable pageable);
}