package com.cdac.enterprise.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

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
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserAdminController.class)
@Import(TestSecurityConfig.class)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getAllUsers_shouldReturn200() throws Exception {
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("9876543210")
                .build();
        Page<User> page = new PageImpl<>(java.util.List.of(user));
        when(userRepository.findByDeletedFalse(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("john@example.com"));
    }

    @Test
    void getUserById_shouldReturn200() throws Exception {
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/admin/users/1")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void getUserById_shouldReturn404WhenNotFound() throws Exception {
        when(userRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/admin/users/99")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchUsersByEmail_shouldReturn200() throws Exception {
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
        when(userRepository.findByEmailAndDeletedFalse("john@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/v1/admin/users/search")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .param("email", "john@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    void searchUsersByEmail_shouldReturn404WhenNotFound() throws Exception {
        when(userRepository.findByEmailAndDeletedFalse("missing@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/admin/users/search")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .param("email", "missing@test.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldReturn200() throws Exception {
        User user = User.builder().id(1L).email("user@test.com").build();
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(delete("/api/v1/admin/users/1")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    // Security constraint tests (403/401) require a full security context setup
    // and are tested separately via integration tests.
}
