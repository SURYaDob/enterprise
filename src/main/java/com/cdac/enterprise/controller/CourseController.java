package com.cdac.enterprise.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.CourseResponse;
import com.cdac.enterprise.service.CourseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Courses", description = "Public APIs for browsing available courses")
@RestController
@RequestMapping(ApiPaths.COURSES)
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(
            summary = "Get active courses",
            description = "Returns paginated list of active and non-deleted courses"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getActiveCourses(
            @ParameterObject
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<CourseResponse> courses = courseService.getActiveCourses(pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.COURSES_FETCHED_SUCCESSFULLY,
                        courses
                ));
    }

    @Operation(
            summary = "Search active courses by name",
            description = "Returns paginated active courses matching the provided name"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> searchActiveCoursesByName(
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<CourseResponse> courses = courseService.searchActiveCoursesByName(name, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.COURSES_FETCHED_SUCCESSFULLY,
                        courses
                ));
    }
}