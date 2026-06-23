package com.cdac.enterprise.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.cdac.enterprise.constant.ApplicationStatus;
import com.cdac.enterprise.constant.AuditAction;
import com.cdac.enterprise.constant.NotificationType;
import com.cdac.enterprise.dto.ApplicationRequest;
import com.cdac.enterprise.dto.ApplicationResponse;
import com.cdac.enterprise.dto.ApplicationReviewRequest;
import com.cdac.enterprise.entity.Application;
import com.cdac.enterprise.entity.Course;
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.exception.ResourceAlreadyExistsException;
import com.cdac.enterprise.exception.ResourceNotFoundException;
import com.cdac.enterprise.repository.ApplicationRepository;
import com.cdac.enterprise.repository.CourseRepository;
import com.cdac.enterprise.repository.UserRepository;
import com.cdac.enterprise.service.AuditLogService;
import com.cdac.enterprise.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    private ApplicationServiceImpl applicationService;

    private User student;
    private User admin;
    private Course course;
    private Application sampleApplication;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationServiceImpl(
                applicationRepository, userRepository, courseRepository,
                notificationService, auditLogService
        );

        student = User.builder()
                .id(1L)
                .firstName("Demo")
                .lastName("Student")
                .email("demo@test.com")
                .build();

        admin = User.builder()
                .id(2L)
                .firstName("System")
                .lastName("Admin")
                .email("admin@test.com")
                .build();

        course = Course.builder()
                .id(1L)
                .code("CDAC-PGDAC")
                .name("PG Diploma in Advanced Computing")
                .active(true)
                .build();

        sampleApplication = Application.builder()
                .id(1L)
                .user(student)
                .course(course)
                .status(ApplicationStatus.SUBMITTED)
                .statementOfPurpose("Test SOP")
                .submittedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    // ========================
    // Create Application
    // ========================

    @Test
    void createApplication_shouldSucceed() {
        ApplicationRequest request = new ApplicationRequest();
        request.setCourseId(1L);
        request.setStatementOfPurpose("I want to learn advanced computing.");

        when(userRepository.findByEmailAndDeletedFalse("demo@test.com")).thenReturn(Optional.of(student));
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
        when(applicationRepository.existsByUserAndCourseAndDeletedFalse(student, course)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(sampleApplication);

        ApplicationResponse response = applicationService.createApplication(
                "demo@test.com", "127.0.0.1", request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(ApplicationStatus.SUBMITTED, response.getStatus());

        verify(notificationService).createNotification(
                eq(student), eq(NotificationType.APPLICATION_SUBMITTED),
                anyString(), anyString()
        );
        verify(auditLogService).logAction(
                eq(student), eq("demo@test.com"), eq(AuditAction.APPLICATION_SUBMITTED),
                eq("APPLICATION"), eq(1L), anyString(), eq("127.0.0.1")
        );
    }

    @Test
    void createApplication_shouldThrowWhenCourseNotFound() {
        ApplicationRequest request = new ApplicationRequest();
        request.setCourseId(99L);

        when(userRepository.findByEmailAndDeletedFalse("demo@test.com")).thenReturn(Optional.of(student));
        when(courseRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.createApplication("demo@test.com", "127.0.0.1", request));
    }

    @Test
    void createApplication_shouldThrowWhenCourseNotActive() {
        course.setActive(false);
        ApplicationRequest request = new ApplicationRequest();
        request.setCourseId(1L);

        when(userRepository.findByEmailAndDeletedFalse("demo@test.com")).thenReturn(Optional.of(student));
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));

        assertThrows(IllegalStateException.class,
                () -> applicationService.createApplication("demo@test.com", "127.0.0.1", request));
    }

    @Test
    void createApplication_shouldThrowWhenDuplicateApplication() {
        ApplicationRequest request = new ApplicationRequest();
        request.setCourseId(1L);

        when(userRepository.findByEmailAndDeletedFalse("demo@test.com")).thenReturn(Optional.of(student));
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
        when(applicationRepository.existsByUserAndCourseAndDeletedFalse(student, course)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> applicationService.createApplication("demo@test.com", "127.0.0.1", request));
    }

    // ========================
    // Get My Applications
    // ========================

    @Test
    void getMyApplications_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Application> page = new PageImpl<>(java.util.List.of(sampleApplication));

        when(userRepository.findByEmailAndDeletedFalse("demo@test.com")).thenReturn(Optional.of(student));
        when(applicationRepository.findByUserAndDeletedFalse(student, pageable)).thenReturn(page);

        Page<ApplicationResponse> result = applicationService.getMyApplications("demo@test.com", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    // ========================
    // Get My Application By ID
    // ========================

    @Test
    void getMyApplicationById_shouldReturnApplication() {
        when(userRepository.findByEmailAndDeletedFalse("demo@test.com")).thenReturn(Optional.of(student));
        when(applicationRepository.findByIdAndUserAndDeletedFalse(1L, student))
                .thenReturn(Optional.of(sampleApplication));

        ApplicationResponse response = applicationService.getMyApplicationById("demo@test.com", 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getMyApplicationById_shouldThrowWhenNotFound() {
        when(userRepository.findByEmailAndDeletedFalse("demo@test.com")).thenReturn(Optional.of(student));
        when(applicationRepository.findByIdAndUserAndDeletedFalse(99L, student))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.getMyApplicationById("demo@test.com", 99L));
    }

    // ========================
    // Review Application
    // ========================

    @Test
    void reviewApplication_shouldUpdateStatus() {
        ApplicationReviewRequest reviewRequest = new ApplicationReviewRequest();
        reviewRequest.setStatus(ApplicationStatus.APPROVED);
        reviewRequest.setAdminRemarks("Looks good, approved.");

        when(applicationRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleApplication));
        when(userRepository.findByEmailAndDeletedFalse("admin@test.com")).thenReturn(Optional.of(admin));

        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = applicationService.reviewApplication(
                "admin@test.com", "10.0.0.1", 1L, reviewRequest);

        assertEquals(ApplicationStatus.APPROVED, response.getStatus());
        assertEquals("Looks good, approved.", response.getAdminRemarks());

        verify(notificationService).createNotification(
                eq(student), eq(NotificationType.APPLICATION_APPROVED),
                anyString(), anyString()
        );
    }

    @Test
    void reviewApplication_shouldThrowWhenApplicationNotFound() {
        ApplicationReviewRequest reviewRequest = new ApplicationReviewRequest();
        reviewRequest.setStatus(ApplicationStatus.REJECTED);

        when(applicationRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationService.reviewApplication("admin@test.com", "10.0.0.1", 99L, reviewRequest));
    }

    // ========================
    // GetAll Applications
    // ========================

    @Test
    void getAllApplications_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Application> page = new PageImpl<>(java.util.List.of(sampleApplication));
        when(applicationRepository.findByDeletedFalse(pageable)).thenReturn(page);

        Page<ApplicationResponse> result = applicationService.getAllApplications(pageable);

        assertEquals(1, result.getTotalElements());
    }

    // ========================
    // Get Applications By Status
    // ========================

    @Test
    void getApplicationsByStatus_shouldFilterByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Application> page = new PageImpl<>(java.util.List.of(sampleApplication));
        when(applicationRepository.findByStatusAndDeletedFalse(ApplicationStatus.SUBMITTED, pageable))
                .thenReturn(page);

        Page<ApplicationResponse> result = applicationService.getApplicationsByStatus(
                ApplicationStatus.SUBMITTED, pageable);

        assertEquals(1, result.getTotalElements());
    }
}
