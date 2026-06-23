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
import com.cdac.enterprise.dto.ApplicationResponse;
import com.cdac.enterprise.dto.ApplicationReviewRequest;
import com.cdac.enterprise.service.ApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ApplicationAdminController.class)
@Import(TestSecurityConfig.class)
class ApplicationAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationService applicationService;

    @Test
    void getAllApplications_shouldReturn200() throws Exception {
        ApplicationResponse app = ApplicationResponse.builder().id(1L).status(ApplicationStatus.SUBMITTED).build();
        Page<ApplicationResponse> page = new PageImpl<>(List.of(app));
        when(applicationService.getAllApplications(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/applications")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].status").value("SUBMITTED"));
    }

    @Test
    void getApplicationById_shouldReturn200() throws Exception {
        ApplicationResponse app = ApplicationResponse.builder().id(1L).status(ApplicationStatus.APPROVED).build();
        when(applicationService.getApplicationById(1L)).thenReturn(app);

        mockMvc.perform(get("/api/v1/admin/applications/1")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void reviewApplication_shouldReturn200() throws Exception {
        ApplicationReviewRequest request = new ApplicationReviewRequest();
        request.setStatus(ApplicationStatus.APPROVED);
        request.setAdminRemarks("Approved.");

        ApplicationResponse response = ApplicationResponse.builder()
                .id(1L)
                .status(ApplicationStatus.APPROVED)
                .adminRemarks("Approved.")
                .build();

        when(applicationService.reviewApplication(anyString(), anyString(), eq(1L), any(ApplicationReviewRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/applications/1/review")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
}
