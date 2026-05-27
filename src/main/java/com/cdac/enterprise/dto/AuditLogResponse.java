package com.cdac.enterprise.dto;

import java.time.LocalDateTime;

import com.cdac.enterprise.constant.AuditAction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private Long actorUserId;
    private String actorEmail;
    private AuditAction action;
    private String entityType;
    private Long entityId;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
}