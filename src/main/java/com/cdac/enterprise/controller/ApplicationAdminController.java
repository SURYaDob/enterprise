package com.cdac.enterprise.controller;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.constant.ApplicationStatus;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.ApplicationResponse;
import com.cdac.enterprise.dto.ApplicationReviewRequest;
import com.cdac.enterprise.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "Admin Applications", description = "Admin APIs for reviewing and managing applications")
@RestController
@RequestMapping(ApiPaths.ADMIN_APPLICATIONS)
public class ApplicationAdminController {

    private final ApplicationService applicationService;

    public ApplicationAdminController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Operation(
            summary = "Get all applications",
            description = "Returns paginated list of all non-deleted applications. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getAllApplications(
            @ParameterObject
            @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<ApplicationResponse> applications = applicationService.getAllApplications(pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.APPLICATIONS_FETCHED_SUCCESSFULLY,
                        applications
                ));
    }

    @Operation(
            summary = "Get application by id",
            description = "Returns one application by id. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(
            @PathVariable Long id
    ) {
        ApplicationResponse application = applicationService.getApplicationById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.APPLICATION_FETCHED_SUCCESSFULLY,
                        application
                ));
    }

    @Operation(
            summary = "Get applications by status",
            description = "Returns paginated applications by status. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getApplicationsByStatus(
            @PathVariable ApplicationStatus status,
            @ParameterObject
            @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<ApplicationResponse> applications = applicationService.getApplicationsByStatus(status, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.APPLICATIONS_FETCHED_SUCCESSFULLY,
                        applications
                ));
    }

    @Operation(
            summary = "Review application",
            description = "Allows admin to update application status and remarks. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/review")
    public ResponseEntity<ApiResponse<ApplicationResponse>> reviewApplication(
            java.security.Principal principal,
            HttpServletRequest servletRequest,
            @PathVariable Long id,
            @Valid @RequestBody ApplicationReviewRequest request
    ) {
        ApplicationResponse application = applicationService.reviewApplication(
                principal.getName(),
                getClientIp(servletRequest),
                id,
                request
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.APPLICATION_UPDATED_SUCCESSFULLY,
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