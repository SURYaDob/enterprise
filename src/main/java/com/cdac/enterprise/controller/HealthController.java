package com.cdac.enterprise.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.HealthResponse;
import com.cdac.enterprise.service.HealthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health Check", description = "APIs for checking application availability")
@RestController
@RequestMapping(ApiPaths.HEALTH)
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @Operation(
            summary = "Check application health",
            description = "Returns application status to confirm that the backend service is running"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<HealthResponse>> getHealthStatus() {
        HealthResponse healthResponse = healthService.getHealthStatus();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.APPLICATION_RUNNING_SUCCESSFULLY,
                        healthResponse
                ));
    }
}