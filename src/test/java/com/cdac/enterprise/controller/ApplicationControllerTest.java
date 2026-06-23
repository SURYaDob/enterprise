package com.cdac.enterprise.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.cdac.enterprise.constant.ApplicationStatus;
import com.cdac.enterprise.dto.ApplicationRequest;
import com.cdac.enterprise.dto.ApplicationResponse;
import com.cdac.enterprise.service.ApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ApplicationController.class)
@Import(TestSecurityConfig.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationService applicationService;

    @Test
    void createApplication_shouldReturn201() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setCourseId(1L);
        request.setStatementOfPurpose("Test SOP");

        ApplicationResponse response = ApplicationResponse.builder()
                .id(1L)
                .courseId(1L)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        when(applicationService.createApplication(anyString(), anyString(), any(ApplicationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/applications")
                        .with(user("student@test.com").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    void getMyApplications_shouldReturn200() throws Exception {
        ApplicationResponse response = ApplicationResponse.builder()
                .id(1L)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        Page<ApplicationResponse> page = new PageImpl<>(List.of(response));
        when(applicationService.getMyApplications(eq("student@test.com"), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/applications")
                        .with(user("student@test.com").roles("STUDENT"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void getMyApplicationById_shouldReturn200() throws Exception {
        ApplicationResponse response = ApplicationResponse.builder()
                .id(1L)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        when(applicationService.getMyApplicationById("student@test.com", 1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/applications/1")
                        .with(user("student@test.com").roles("STUDENT"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }


}
