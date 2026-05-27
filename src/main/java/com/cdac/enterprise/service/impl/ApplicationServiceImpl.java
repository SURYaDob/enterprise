package com.cdac.enterprise.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.enterprise.constant.ApplicationStatus;
import com.cdac.enterprise.constant.AppMessages;
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
import com.cdac.enterprise.service.ApplicationService;
import com.cdac.enterprise.service.AuditLogService;
import com.cdac.enterprise.service.NotificationService;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final String ENTITY_TYPE_APPLICATION = "APPLICATION";

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ApplicationServiceImpl(
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            NotificationService notificationService,
            AuditLogService auditLogService
    ) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public ApplicationResponse createApplication(String studentEmail, String ipAddress, ApplicationRequest request) {
        User student = getStudentByEmail(studentEmail);

        Course course = courseRepository.findByIdAndDeletedFalse(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.COURSE_NOT_FOUND));

        if (!Boolean.TRUE.equals(course.getActive())) {
            throw new IllegalStateException(AppMessages.COURSE_NOT_ACTIVE);
        }

        if (applicationRepository.existsByUserAndCourseAndDeletedFalse(student, course)) {
            throw new ResourceAlreadyExistsException(AppMessages.APPLICATION_ALREADY_EXISTS);
        }

        Application application = Application.builder()
                .user(student)
                .course(course)
                .statementOfPurpose(request.getStatementOfPurpose())
                .build();

        Application savedApplication = applicationRepository.save(application);

        notificationService.createNotification(
                student,
                NotificationType.APPLICATION_SUBMITTED,
                "Application submitted",
                "Your application for " + course.getCode() + " has been submitted successfully."
        );

        auditLogService.logAction(
                student,
                studentEmail,
                AuditAction.APPLICATION_SUBMITTED,
                ENTITY_TYPE_APPLICATION,
                savedApplication.getId(),
                "Application submitted for course: " + course.getCode(),
                ipAddress
        );

        return mapToApplicationResponse(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(String studentEmail, Pageable pageable) {
        User student = getStudentByEmail(studentEmail);

        return applicationRepository.findByUserAndDeletedFalse(student, pageable)
                .map(this::mapToApplicationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getMyApplicationById(String studentEmail, Long applicationId) {
        User student = getStudentByEmail(studentEmail);

        Application application = applicationRepository
                .findByIdAndUserAndDeletedFalse(applicationId, student)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.APPLICATION_NOT_FOUND));

        return mapToApplicationResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getAllApplications(Pageable pageable) {
        return applicationRepository.findByDeletedFalse(pageable)
                .map(this::mapToApplicationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId) {
        Application application = getApplicationEntityById(applicationId);

        return mapToApplicationResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByStatus(ApplicationStatus status, Pageable pageable) {
        return applicationRepository.findByStatusAndDeletedFalse(status, pageable)
                .map(this::mapToApplicationResponse);
    }

    @Override
    @Transactional
    public ApplicationResponse reviewApplication(
            String actorEmail,
            String ipAddress,
            Long applicationId,
            ApplicationReviewRequest request
    ) {
        Application application = getApplicationEntityById(applicationId);

        application.setStatus(request.getStatus());
        application.setAdminRemarks(request.getAdminRemarks());
        application.setReviewedAt(LocalDateTime.now());

        Application reviewedApplication = applicationRepository.save(application);

        notificationService.createNotification(
                reviewedApplication.getUser(),
                resolveNotificationType(request.getStatus()),
                "Application status updated",
                "Your application for " + reviewedApplication.getCourse().getCode()
                        + " is now " + request.getStatus().name() + "."
        );

        auditLogService.logAction(
                getActorUser(actorEmail),
                actorEmail,
                AuditAction.APPLICATION_REVIEWED,
                ENTITY_TYPE_APPLICATION,
                reviewedApplication.getId(),
                "Application reviewed with status: " + request.getStatus().name(),
                ipAddress
        );

        return mapToApplicationResponse(reviewedApplication);
    }

    private NotificationType resolveNotificationType(ApplicationStatus status) {
        return switch (status) {
            case APPROVED -> NotificationType.APPLICATION_APPROVED;
            case REJECTED -> NotificationType.APPLICATION_REJECTED;
            case UNDER_REVIEW -> NotificationType.APPLICATION_UNDER_REVIEW;
            default -> NotificationType.APPLICATION_UNDER_REVIEW;
        };
    }

    private User getStudentByEmail(String studentEmail) {
        return userRepository.findByEmailAndDeletedFalse(studentEmail.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.USER_NOT_FOUND));
    }

    private User getActorUser(String actorEmail) {
        return userRepository.findByEmailAndDeletedFalse(actorEmail.toLowerCase())
                .orElse(null);
    }

    private Application getApplicationEntityById(Long applicationId) {
        return applicationRepository.findByIdAndDeletedFalse(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.APPLICATION_NOT_FOUND));
    }

    private ApplicationResponse mapToApplicationResponse(Application application) {
        User student = application.getUser();
        Course course = application.getCourse();

        return ApplicationResponse.builder()
                .id(application.getId())
                .userId(student.getId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .studentEmail(student.getEmail())
                .courseId(course.getId())
                .courseCode(course.getCode())
                .courseName(course.getName())
                .status(application.getStatus())
                .statementOfPurpose(application.getStatementOfPurpose())
                .adminRemarks(application.getAdminRemarks())
                .submittedAt(application.getSubmittedAt())
                .reviewedAt(application.getReviewedAt())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}