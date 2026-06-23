package com.cdac.enterprise.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cdac.enterprise.config.TestSecurityConfig;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.dto.ForgotPasswordRequest;
import com.cdac.enterprise.dto.LoginRequest;
import com.cdac.enterprise.dto.LoginResponse;
import com.cdac.enterprise.dto.RegisterRequest;
import com.cdac.enterprise.dto.ResetPasswordRequest;
import com.cdac.enterprise.dto.UserResponse;
import com.cdac.enterprise.exception.ResourceNotFoundException;
import com.cdac.enterprise.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // ========================
    // Register
    // ========================

    @Test
    void register_shouldReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("Str0ng!Pass");
        request.setPhoneNumber("9876543210");

        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("9876543210")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value(AppMessages.USER_REGISTERED_SUCCESSFULLY))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void register_shouldReturn400WhenValidationFails() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid");
        request.setPassword("short");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========================
    // Login
    // ========================

    @Test
    void login_shouldReturn200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("Str0ng!Pass");

        LoginResponse loginResponse = LoginResponse.builder()
                .id(1L)
                .email("john@example.com")
                .roles(Set.of("ROLE_STUDENT"))
                .accessToken("jwt-token")
                .tokenType("Bearer")
                .expiresIn(900)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void login_shouldReturn400WhenValidationFails() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email");
        request.setPassword("");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========================
    // Forgot Password
    // ========================

    @Test
    void forgotPassword_shouldReturn200() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("john@example.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(AppMessages.RESET_TOKEN_SENT));
    }

    @Test
    void forgotPassword_shouldReturn404WhenEmailNotFound() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@example.com");

        doThrow(new ResourceNotFoundException(AppMessages.USER_NOT_FOUND_WITH_EMAIL))
                .when(authService).forgotPassword(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ========================
    // Reset Password
    // ========================

    @Test
    void resetPassword_shouldReturn200() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("NewStr0ng!Pass");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(AppMessages.PASSWORD_RESET_SUCCESSFULLY));
    }
}
