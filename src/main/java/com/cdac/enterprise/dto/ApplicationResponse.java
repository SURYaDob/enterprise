package com.cdac.enterprise.dto;

import java.time.LocalDateTime;

import com.cdac.enterprise.constant.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private Long id;

    private Long userId;
    private String studentName;
    private String studentEmail;

    private Long courseId;
    private String courseCode;
    private String courseName;

    private ApplicationStatus status;
    private String statementOfPurpose;
    private String adminRemarks;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}