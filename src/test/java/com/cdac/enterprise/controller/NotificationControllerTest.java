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
import com.cdac.enterprise.constant.NotificationType;
import com.cdac.enterprise.dto.NotificationResponse;
import com.cdac.enterprise.service.NotificationService;

@WebMvcTest(NotificationController.class)
@Import(TestSecurityConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void getMyNotifications_shouldReturn200() throws Exception {
        NotificationResponse notification = NotificationResponse.builder()
                .id(1L)
                .type(NotificationType.APPLICATION_SUBMITTED)
                .title("Application Submitted")
                .readStatus(false)
                .build();

        Page<NotificationResponse> page = new PageImpl<>(List.of(notification));
        when(notificationService.getMyNotifications(eq("student@test.com"), eq(false), any(PageRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications")
                        .with(user("student@test.com").roles("STUDENT"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Application Submitted"));
    }

    @Test
    void markAsRead_shouldReturn200() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .readStatus(true)
                .build();

        when(notificationService.markAsRead("student@test.com", 1L)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/notifications/1/read")
                        .with(user("student@test.com").roles("STUDENT"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readStatus").value(true));
    }
}
