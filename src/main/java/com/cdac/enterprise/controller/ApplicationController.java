package com.cdac.enterprise.controller;

import java.security.Principal;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.ApplicationRequest;
import com.cdac.enterprise.dto.ApplicationResponse;
import com.cdac.enterprise.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "Applications", description = "Student APIs for course applications")
@RestController
@RequestMapping(ApiPaths.APPLICATIONS)
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Operation(
            summary = "Submit course application",
            description = "Allows a student to submit an application for an active course"
    )
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
            Principal principal,
            HttpServletRequest servletRequest,
            @Valid @RequestBody ApplicationRequest request
    ) {
        ApplicationResponse applicationResponse = applicationService.createApplication(
                principal.getName(),
                getClientIp(servletRequest),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        AppMessages.APPLICATION_SUBMITTED_SUCCESSFULLY,
                        applicationResponse
                ));
    }

    @Operation(
            summary = "Get my applications",
            description = "Returns paginated applications submitted by the logged-in student"
    )
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getMyApplications(
            Principal principal,
            @ParameterObject
            @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<ApplicationResponse> applications = applicationService.getMyApplications(
                principal.getName(),
                pageable
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.APPLICATIONS_FETCHED_SUCCESSFULLY,
                        applications
                ));
    }

    @Operation(
            summary = "Get my application by id",
            description = "Returns one application only if it belongs to the logged-in student"
    )
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getMyApplicationById(
            Principal principal,
            @PathVariable Long id
    ) {
        ApplicationResponse application = applicationService.getMyApplicationById(
                principal.getName(),
                id
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.APPLICATION_FETCHED_SUCCESSFULLY,
                        application
                ));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}