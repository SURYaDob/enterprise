package com.cdac.enterprise.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cdac.enterprise.config.TestSecurityConfig;
import com.cdac.enterprise.dto.CourseRequest;
import com.cdac.enterprise.dto.CourseResponse;
import com.cdac.enterprise.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CourseAdminController.class)
@Import(TestSecurityConfig.class)
class CourseAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    @Test
    void createCourse_shouldReturn201() throws Exception {
        CourseRequest request = new CourseRequest();
        request.setCode("CDAC-NEW");
        request.setName("New Course");
        request.setDurationInMonths(6);
        request.setFee(new BigDecimal("50000.00"));
        request.setCapacity(60);
        request.setActive(true);

        CourseResponse response = CourseResponse.builder()
                .id(1L)
                .code("CDAC-NEW")
                .name("New Course")
                .durationInMonths(6)
                .build();

        when(courseService.createCourse(anyString(), anyString(), any(CourseRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/courses")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("CDAC-NEW"));
    }

    @Test
    void getAllCourses_shouldReturn200() throws Exception {
        CourseResponse course = CourseResponse.builder().id(1L).code("CDAC-TEST").name("Test").build();
        Page<CourseResponse> page = new PageImpl<>(List.of(course));
        when(courseService.getAllCourses(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/courses")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("CDAC-TEST"));
    }

    @Test
    void deleteCourse_shouldReturn200() throws Exception {
        doNothing().when(courseService).deleteCourse(anyString(), anyString(), eq(1L));

        mockMvc.perform(delete("/api/v1/admin/courses/1")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Course deleted successfully"));
    }
}
