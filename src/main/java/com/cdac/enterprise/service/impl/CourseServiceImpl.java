package com.cdac.enterprise.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.constant.AuditAction;
import com.cdac.enterprise.dto.CourseRequest;
import com.cdac.enterprise.dto.CourseResponse;
import com.cdac.enterprise.entity.Course;
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.exception.ResourceAlreadyExistsException;
import com.cdac.enterprise.exception.ResourceNotFoundException;
import com.cdac.enterprise.repository.CourseRepository;
import com.cdac.enterprise.repository.UserRepository;
import com.cdac.enterprise.service.AuditLogService;
import com.cdac.enterprise.service.CourseService;

@Service
public class CourseServiceImpl implements CourseService {

    private static final String ENTITY_TYPE_COURSE = "COURSE";

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public CourseServiceImpl(
            CourseRepository courseRepository,
            UserRepository userRepository,
            AuditLogService auditLogService
    ) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public CourseResponse createCourse(String actorEmail, String ipAddress, CourseRequest request) {
        String normalizedCode = request.getCode().trim().toUpperCase();

        if (courseRepository.existsByCodeAndDeletedFalse(normalizedCode)) {
            throw new ResourceAlreadyExistsException(AppMessages.COURSE_CODE_ALREADY_EXISTS);
        }

        Course course = Course.builder()
                .code(normalizedCode)
                .name(request.getName().trim())
                .description(request.getDescription())
                .durationInMonths(request.getDurationInMonths())
                .fee(request.getFee())
                .capacity(request.getCapacity())
                .active(request.getActive())
                .build();

        Course savedCourse = courseRepository.save(course);

        auditLogService.logAction(
                getActorUser(actorEmail),
                actorEmail,
                AuditAction.COURSE_CREATED,
                ENTITY_TYPE_COURSE,
                savedCourse.getId(),
                "Course created: " + savedCourse.getCode(),
                ipAddress
        );

        return mapToCourseResponse(savedCourse);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(String actorEmail, String ipAddress, Long id, CourseRequest request) {
        Course course = getCourseEntityById(id);
        String normalizedCode = request.getCode().trim().toUpperCase();

        if (!course.getCode().equals(normalizedCode)
                && courseRepository.existsByCodeAndDeletedFalse(normalizedCode)) {
            throw new ResourceAlreadyExistsException(AppMessages.COURSE_CODE_ALREADY_EXISTS);
        }

        course.setCode(normalizedCode);
        course.setName(request.getName().trim());
        course.setDescription(request.getDescription());
        course.setDurationInMonths(request.getDurationInMonths());
        course.setFee(request.getFee());
        course.setCapacity(request.getCapacity());
        course.setActive(request.getActive());

        Course updatedCourse = courseRepository.save(course);

        auditLogService.logAction(
                getActorUser(actorEmail),
                actorEmail,
                AuditAction.COURSE_UPDATED,
                ENTITY_TYPE_COURSE,
                updatedCourse.getId(),
                "Course updated: " + updatedCourse.getCode(),
                ipAddress
        );

        return mapToCourseResponse(updatedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        return mapToCourseResponse(getCourseEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllCourses(Pageable pageable) {
        return courseRepository.findByDeletedFalse(pageable)
                .map(this::mapToCourseResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getActiveCourses(Pageable pageable) {
        return courseRepository.findByActiveTrueAndDeletedFalse(pageable)
                .map(this::mapToCourseResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> searchActiveCoursesByName(String name, Pageable pageable) {
        return courseRepository.findByNameContainingIgnoreCaseAndActiveTrueAndDeletedFalse(
                        name.trim(),
                        pageable
                )
                .map(this::mapToCourseResponse);
    }

    @Override
    @Transactional
    public void deleteCourse(String actorEmail, String ipAddress, Long id) {
        Course course = getCourseEntityById(id);

        course.setActive(false);
        course.setDeleted(true);

        Course deletedCourse = courseRepository.save(course);

        auditLogService.logAction(
                getActorUser(actorEmail),
                actorEmail,
                AuditAction.COURSE_DELETED,
                ENTITY_TYPE_COURSE,
                deletedCourse.getId(),
                "Course deleted: " + deletedCourse.getCode(),
                ipAddress
        );
    }

    private Course getCourseEntityById(Long id) {
        return courseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.COURSE_NOT_FOUND));
    }

    private User getActorUser(String actorEmail) {
        return userRepository.findByEmailAndDeletedFalse(actorEmail.toLowerCase())
                .orElse(null);
    }

    private CourseResponse mapToCourseResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .description(course.getDescription())
                .durationInMonths(course.getDurationInMonths())
                .fee(course.getFee())
                .capacity(course.getCapacity())
                .active(course.getActive())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}