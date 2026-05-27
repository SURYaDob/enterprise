package com.cdac.enterprise.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.enterprise.constant.ApplicationStatus;
import com.cdac.enterprise.dto.DashboardStatsResponse;
import com.cdac.enterprise.repository.ApplicationRepository;
import com.cdac.enterprise.repository.CourseRepository;
import com.cdac.enterprise.repository.UploadedDocumentRepository;
import com.cdac.enterprise.repository.UserRepository;
import com.cdac.enterprise.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ApplicationRepository applicationRepository;
    private final UploadedDocumentRepository uploadedDocumentRepository;

    public DashboardServiceImpl(
            UserRepository userRepository,
            CourseRepository courseRepository,
            ApplicationRepository applicationRepository,
            UploadedDocumentRepository uploadedDocumentRepository
    ) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.applicationRepository = applicationRepository;
        this.uploadedDocumentRepository = uploadedDocumentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.countByDeletedFalse())
                .totalCourses(courseRepository.countByDeletedFalse())
                .totalApplications(applicationRepository.countByDeletedFalse())
                .submittedApplications(applicationRepository.countByStatusAndDeletedFalse(ApplicationStatus.SUBMITTED))
                .approvedApplications(applicationRepository.countByStatusAndDeletedFalse(ApplicationStatus.APPROVED))
                .rejectedApplications(applicationRepository.countByStatusAndDeletedFalse(ApplicationStatus.REJECTED))
                .totalUploadedDocuments(uploadedDocumentRepository.countByDeletedFalse())
                .build();
    }
}