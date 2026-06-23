package com.cdac.enterprise.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cdac.enterprise.config.TestSecurityConfig;
import com.cdac.enterprise.dto.HealthResponse;
import com.cdac.enterprise.service.HealthService;

@WebMvcTest(HealthController.class)
@Import(TestSecurityConfig.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthService healthService;

    @Test
    void getHealthStatus_shouldReturn200() throws Exception {
        when(healthService.getHealthStatus())
                .thenReturn(HealthResponse.builder()
                        .application("CDAC Enterprise Application")
                        .status("UP")
                        .build());

        mockMvc.perform(get("/api/v1/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Application is running successfully"))
                .andExpect(jsonPath("$.data.application").value("CDAC Enterprise Application"))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
