package com.cdac.enterprise.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cdac.enterprise.constant.ApplicationStatus;
import com.cdac.enterprise.dto.DashboardStatsResponse;
import com.cdac.enterprise.repository.ApplicationRepository;
import com.cdac.enterprise.repository.CourseRepository;
import com.cdac.enterprise.repository.UploadedDocumentRepository;
import com.cdac.enterprise.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UploadedDocumentRepository uploadedDocumentRepository;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(
                userRepository, courseRepository, applicationRepository, uploadedDocumentRepository
        );
    }

    @Test
    void getDashboardStats_shouldReturnAggregatedStats() {
        when(userRepository.countByDeletedFalse()).thenReturn(10L);
        when(courseRepository.countByDeletedFalse()).thenReturn(5L);
        when(applicationRepository.countByDeletedFalse()).thenReturn(20L);
        when(applicationRepository.countByStatusAndDeletedFalse(ApplicationStatus.SUBMITTED)).thenReturn(8L);
        when(applicationRepository.countByStatusAndDeletedFalse(ApplicationStatus.APPROVED)).thenReturn(7L);
        when(applicationRepository.countByStatusAndDeletedFalse(ApplicationStatus.REJECTED)).thenReturn(3L);
        when(uploadedDocumentRepository.countByDeletedFalse()).thenReturn(15L);

        DashboardStatsResponse stats = dashboardService.getDashboardStats();

        assertEquals(10L, stats.getTotalUsers());
        assertEquals(5L, stats.getTotalCourses());
        assertEquals(20L, stats.getTotalApplications());
        assertEquals(8L, stats.getSubmittedApplications());
        assertEquals(7L, stats.getApprovedApplications());
        assertEquals(3L, stats.getRejectedApplications());
        assertEquals(15L, stats.getTotalUploadedDocuments());
    }
}
