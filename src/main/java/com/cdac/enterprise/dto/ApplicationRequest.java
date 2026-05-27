package com.cdac.enterprise.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequest {

    @NotNull(message = "Course id is required")
    private Long courseId;

    @Size(max = 5000, message = "Statement of purpose must not exceed 5000 characters")
    private String statementOfPurpose;
}