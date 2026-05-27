package com.cdac.enterprise.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer durationInMonths;
    private BigDecimal fee;
    private Integer capacity;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}