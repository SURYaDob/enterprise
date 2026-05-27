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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.CourseRequest;
import com.cdac.enterprise.dto.CourseResponse;
import com.cdac.enterprise.service.CourseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Tag(name = "Admin Courses", description = "Admin APIs for managing courses")
@RestController
@RequestMapping(ApiPaths.ADMIN_COURSES)
public class CourseAdminController {

    private final CourseService courseService;

    public CourseAdminController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(
            summary = "Create course",
            description = "Creates a new course. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            Principal principal,
            HttpServletRequest servletRequest,
            @Valid @RequestBody CourseRequest request
    ) {
        CourseResponse courseResponse = courseService.createCourse(
                principal.getName(),
                getClientIp(servletRequest),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        AppMessages.COURSE_CREATED_SUCCESSFULLY,
                        courseResponse
                ));
    }

    @Operation(
            summary = "Get all courses",
            description = "Returns paginated list of all non-deleted courses. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getAllCourses(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<CourseResponse> courses = courseService.getAllCourses(pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.COURSES_FETCHED_SUCCESSFULLY,
                        courses
                ));
    }

    @Operation(
            summary = "Get course by id",
            description = "Returns a course by id. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(
            @PathVariable Long id
    ) {
        CourseResponse courseResponse = courseService.getCourseById(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.COURSE_FETCHED_SUCCESSFULLY,
                        courseResponse
                ));
    }

    @Operation(
            summary = "Update course",
            description = "Updates course details. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            Principal principal,
            HttpServletRequest servletRequest,
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request
    ) {
        CourseResponse courseResponse = courseService.updateCourse(
                principal.getName(),
                getClientIp(servletRequest),
                id,
                request
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.COURSE_UPDATED_SUCCESSFULLY,
                        courseResponse
                ));
    }

    @Operation(
            summary = "Delete course",
            description = "Soft deletes a course by marking deleted=true and active=false. Requires ROLE_ADMIN."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteCourse(
            Principal principal,
            HttpServletRequest servletRequest,
            @PathVariable Long id
    ) {
        courseService.deleteCourse(
                principal.getName(),
                getClientIp(servletRequest),
                id
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.COURSE_DELETED_SUCCESSFULLY,
                        null
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