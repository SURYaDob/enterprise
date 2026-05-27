package com.cdac.enterprise.dto;

import com.cdac.enterprise.constant.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationReviewRequest {

    @NotNull(message = "Application status is required")
    private ApplicationStatus status;

    @Size(max = 5000, message = "Admin remarks must not exceed 5000 characters")
    private String adminRemarks;
}