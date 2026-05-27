package com.cdac.enterprise.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cdac.enterprise.dto.CourseRequest;
import com.cdac.enterprise.dto.CourseResponse;

public interface CourseService {

    CourseResponse createCourse(String actorEmail, String ipAddress, CourseRequest request);

    CourseResponse updateCourse(String actorEmail, String ipAddress, Long id, CourseRequest request);

    CourseResponse getCourseById(Long id);

    Page<CourseResponse> getAllCourses(Pageable pageable);

    Page<CourseResponse> getActiveCourses(Pageable pageable);

    Page<CourseResponse> searchActiveCoursesByName(String name, Pageable pageable);

    void deleteCourse(String actorEmail, String ipAddress, Long id);
}