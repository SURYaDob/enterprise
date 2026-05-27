package com.cdac.enterprise.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cdac.enterprise.constant.ApplicationStatus;
import com.cdac.enterprise.dto.ApplicationRequest;
import com.cdac.enterprise.dto.ApplicationResponse;
import com.cdac.enterprise.dto.ApplicationReviewRequest;

public interface ApplicationService {

    ApplicationResponse createApplication(String studentEmail, String ipAddress, ApplicationRequest request);

    Page<ApplicationResponse> getMyApplications(String studentEmail, Pageable pageable);

    ApplicationResponse getMyApplicationById(String studentEmail, Long applicationId);

    Page<ApplicationResponse> getAllApplications(Pageable pageable);

    ApplicationResponse getApplicationById(Long applicationId);

    Page<ApplicationResponse> getApplicationsByStatus(ApplicationStatus status, Pageable pageable);

    ApplicationResponse reviewApplication(String actorEmail, String ipAddress, Long applicationId, ApplicationReviewRequest request);
}