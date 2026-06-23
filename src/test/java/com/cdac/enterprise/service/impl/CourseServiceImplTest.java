package com.cdac.enterprise.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private CourseServiceImpl courseService;

    private CourseRequest validRequest;
    private Course sampleCourse;

    @BeforeEach
    void setUp() {
        courseService = new CourseServiceImpl(courseRepository, userRepository, auditLogService);

        validRequest = new CourseRequest();
        validRequest.setCode("CDAC-TEST");
        validRequest.setName("Test Course");
        validRequest.setDescription("A test course description");
        validRequest.setDurationInMonths(6);
        validRequest.setFee(new BigDecimal("50000.00"));
        validRequest.setCapacity(60);
        validRequest.setActive(true);

        sampleCourse = Course.builder()
                .id(1L)
                .code("CDAC-TEST")
                .name("Test Course")
                .description("A test course description")
                .durationInMonths(6)
                .fee(new BigDecimal("50000.00"))
                .capacity(60)
                .active(true)
                .deleted(false)
                .build();
    }

    // ========================
    // Create Course
    // ========================

    @Test
    void createCourse_shouldSucceed() {
        when(courseRepository.existsByCodeAndDeletedFalse("CDAC-TEST")).thenReturn(false);
        when(userRepository.findByEmailAndDeletedFalse("admin@test.com")).thenReturn(Optional.of(mock(User.class)));
        when(courseRepository.save(any(Course.class))).thenReturn(sampleCourse);

        CourseResponse response = courseService.createCourse("admin@test.com", "127.0.0.1", validRequest);

        assertNotNull(response);
        assertEquals("CDAC-TEST", response.getCode());
        assertEquals("Test Course", response.getName());
        assertEquals(6, response.getDurationInMonths());
        assertEquals(new BigDecimal("50000.00"), response.getFee());
        assertEquals(60, response.getCapacity());
        assertTrue(response.getActive());

        verify(auditLogService).logAction(
                any(), eq("admin@test.com"), eq(AuditAction.COURSE_CREATED),
                eq("COURSE"), eq(1L), anyString(), eq("127.0.0.1")
        );
    }

    @Test
    void createCourse_shouldThrowWhenCodeAlreadyExists() {
        when(courseRepository.existsByCodeAndDeletedFalse("CDAC-TEST")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> courseService.createCourse("admin@test.com", "127.0.0.1", validRequest));
        verify(courseRepository, never()).save(any());
    }

    @Test
    void createCourse_shouldNormalizeCodeToUppercase() {
        validRequest.setCode(" cdac-test ");
        when(courseRepository.existsByCodeAndDeletedFalse("CDAC-TEST")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(sampleCourse);
        when(userRepository.findByEmailAndDeletedFalse(anyString())).thenReturn(Optional.of(mock(User.class)));

        courseService.createCourse("admin@test.com", "127.0.0.1", validRequest);

        verify(courseRepository).existsByCodeAndDeletedFalse("CDAC-TEST");
    }

    // ========================
    // Get Course By ID
    // ========================

    @Test
    void getCourseById_shouldReturnCourse() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCourse));

        CourseResponse response = courseService.getCourseById(1L);

        assertNotNull(response);
        assertEquals("CDAC-TEST", response.getCode());
        assertEquals("Test Course", response.getName());
    }

    @Test
    void getCourseById_shouldThrowWhenNotFound() {
        when(courseRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(99L));
    }

    // ========================
    // GetAll Courses
    // ========================

    @Test
    void getAllCourses_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(java.util.List.of(sampleCourse));
        when(courseRepository.findByDeletedFalse(pageable)).thenReturn(page);

        Page<CourseResponse> result = courseService.getAllCourses(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("CDAC-TEST", result.getContent().get(0).getCode());
    }

    // ========================
    // Get Active Courses
    // ========================

    @Test
    void getActiveCourses_shouldReturnOnlyActive() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(java.util.List.of(sampleCourse));
        when(courseRepository.findByActiveTrueAndDeletedFalse(pageable)).thenReturn(page);

        Page<CourseResponse> result = courseService.getActiveCourses(pageable);

        assertEquals(1, result.getTotalElements());
    }

    // ========================
    // Search Active Courses
    // ========================

    @Test
    void searchActiveCoursesByName_shouldReturnMatching() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(java.util.List.of(sampleCourse));
        when(courseRepository.findByNameContainingIgnoreCaseAndActiveTrueAndDeletedFalse("test", pageable))
                .thenReturn(page);

        Page<CourseResponse> result = courseService.searchActiveCoursesByName("test", pageable);

        assertEquals(1, result.getTotalElements());
    }

    // ========================
    // Update Course
    // ========================

    @Test
    void updateCourse_shouldUpdateSuccessfully() {
        CourseRequest updateRequest = new CourseRequest();
        updateRequest.setCode("CDAC-UPDATED");
        updateRequest.setName("Updated Course");
        updateRequest.setDescription("Updated description");
        updateRequest.setDurationInMonths(9);
        updateRequest.setFee(new BigDecimal("75000.00"));
        updateRequest.setCapacity(120);
        updateRequest.setActive(true);

        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCourse));
        when(courseRepository.existsByCodeAndDeletedFalse("CDAC-UPDATED")).thenReturn(false);
        when(userRepository.findByEmailAndDeletedFalse(anyString())).thenReturn(Optional.of(mock(User.class)));

        Course updatedCourse = Course.builder()
                .id(1L)
                .code("CDAC-UPDATED")
                .name("Updated Course")
                .description("Updated description")
                .durationInMonths(9)
                .fee(new BigDecimal("75000.00"))
                .capacity(120)
                .active(true)
                .build();
        when(courseRepository.save(any(Course.class))).thenReturn(updatedCourse);

        CourseResponse response = courseService.updateCourse("admin@test.com", "127.0.0.1", 1L, updateRequest);

        assertEquals("CDAC-UPDATED", response.getCode());
        assertEquals("Updated Course", response.getName());
        assertEquals(9, response.getDurationInMonths());
        assertEquals(new BigDecimal("75000.00"), response.getFee());
        assertEquals(120, response.getCapacity());

        verify(auditLogService).logAction(
                any(), eq("admin@test.com"), eq(AuditAction.COURSE_UPDATED),
                eq("COURSE"), eq(1L), anyString(), eq("127.0.0.1")
        );
    }

    // ========================
    // Delete Course
    // ========================

    @Test
    void deleteCourse_shouldSoftDelete() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(sampleCourse));
        when(userRepository.findByEmailAndDeletedFalse(anyString())).thenReturn(Optional.of(mock(User.class)));
        when(courseRepository.save(any(Course.class))).thenReturn(sampleCourse);

        courseService.deleteCourse("admin@test.com", "127.0.0.1", 1L);

        verify(auditLogService).logAction(
                any(), eq("admin@test.com"), eq(AuditAction.COURSE_DELETED),
                eq("COURSE"), eq(1L), anyString(), eq("127.0.0.1")
        );
    }
}
