package com.cdac.enterprise.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
import com.cdac.enterprise.dto.CourseResponse;
import com.cdac.enterprise.service.CourseService;

@WebMvcTest(CourseController.class)
@Import(TestSecurityConfig.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @Test
    void getActiveCourses_shouldReturn200() throws Exception {
        CourseResponse course = CourseResponse.builder()
                .id(1L)
                .code("CDAC-PGDAC")
                .name("PG Diploma in Advanced Computing")
                .durationInMonths(9)
                .fee(new BigDecimal("150000.00"))
                .capacity(240)
                .active(true)
                .build();

        Page<CourseResponse> page = new PageImpl<>(List.of(course));
        when(courseService.getActiveCourses(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/courses")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].code").value("CDAC-PGDAC"));
    }

    @Test
    void searchCourses_shouldReturn200() throws Exception {
        CourseResponse course = CourseResponse.builder()
                .id(2L)
                .code("CDAC-DAC")
                .name("Diploma in Advanced Computing")
                .active(true)
                .build();

        Page<CourseResponse> page = new PageImpl<>(List.of(course));
        when(courseService.searchActiveCoursesByName(eq("dac"), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/courses/search")
                        .param("name", "dac")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("CDAC-DAC"));
    }
}
