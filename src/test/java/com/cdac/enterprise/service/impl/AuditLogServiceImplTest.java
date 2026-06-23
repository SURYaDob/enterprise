package com.cdac.enterprise.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.cdac.enterprise.constant.AuditAction;
import com.cdac.enterprise.dto.AuditLogResponse;
import com.cdac.enterprise.entity.AuditLog;
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.repository.AuditLogRepository;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogServiceImpl auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogServiceImpl(auditLogRepository);
    }

    @Test
    void logAction_shouldSaveAuditLog() {
        User actor = User.builder().id(1L).email("admin@test.com").build();

        auditLogService.logAction(
                actor, "admin@test.com", AuditAction.COURSE_CREATED,
                "COURSE", 1L, "Course created: CDAC-TEST", "127.0.0.1"
        );

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logAction_shouldSaveWithNullActor() {
        auditLogService.logAction(
                null, "system@test.com", AuditAction.APPLICATION_REVIEWED,
                "APPLICATION", 1L, "Reviewed", "10.0.0.1"
        );

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getAuditLogs_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .actorEmail("admin@test.com")
                .action(AuditAction.COURSE_CREATED)
                .entityType("COURSE")
                .entityId(1L)
                .description("Course created")
                .ipAddress("127.0.0.1")
                .build();

        Page<AuditLog> page = new PageImpl<>(List.of(auditLog));
        when(auditLogRepository.findAll(pageable)).thenReturn(page);

        Page<AuditLogResponse> result = auditLogService.getAuditLogs(pageable);

        assertEquals(1, result.getTotalElements());
        AuditLogResponse response = result.getContent().get(0);
        assertEquals(1L, response.getId());
        assertEquals("admin@test.com", response.getActorEmail());
        assertEquals(AuditAction.COURSE_CREATED, response.getAction());
        assertEquals("COURSE", response.getEntityType());
    }
}
