package com.cdac.enterprise.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.enterprise.constant.AuditAction;
import com.cdac.enterprise.dto.AuditLogResponse;
import com.cdac.enterprise.entity.AuditLog;
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.repository.AuditLogRepository;
import com.cdac.enterprise.service.AuditLogService;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void logAction(
            User actorUser,
            String actorEmail,
            AuditAction action,
            String entityType,
            Long entityId,
            String description,
            String ipAddress
    ) {
        AuditLog auditLog = AuditLog.builder()
                .actorUser(actorUser)
                .actorEmail(actorEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(this::mapToAuditLogResponse);
    }

    private AuditLogResponse mapToAuditLogResponse(AuditLog auditLog) {
        Long actorUserId = auditLog.getActorUser() != null
                ? auditLog.getActorUser().getId()
                : null;

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .actorUserId(actorUserId)
                .actorEmail(auditLog.getActorEmail())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .description(auditLog.getDescription())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}