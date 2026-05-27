package com.cdac.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalUsers;
    private long totalCourses;
    private long totalApplications;
    private long submittedApplications;
    private long approvedApplications;
    private long rejectedApplications;
    private long totalUploadedDocuments;
}