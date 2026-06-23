package com.cdac.enterprise.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.constant.RoleName;
import com.cdac.enterprise.dto.ForgotPasswordRequest;
import com.cdac.enterprise.dto.LoginRequest;
import com.cdac.enterprise.dto.RegisterRequest;
import com.cdac.enterprise.dto.ResetPasswordRequest;
import com.cdac.enterprise.dto.UserResponse;
import com.cdac.enterprise.entity.Role;
import com.cdac.enterprise.entity.User;
import com.cdac.enterprise.exception.ResourceAlreadyExistsException;
import com.cdac.enterprise.exception.ResourceNotFoundException;
import com.cdac.enterprise.repository.RoleRepository;
import com.cdac.enterprise.repository.UserRepository;
import com.cdac.enterprise.security.JwtService;
import com.cdac.enterprise.service.EmailService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    private AuthServiceImpl authService;

    private Role studentRole;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                roleRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                emailService
        );

        studentRole = Role.builder()
                .id(1L)
                .name(RoleName.ROLE_STUDENT)
                .description("Student role")
                .build();
    }

    // ========================
    // Register
    // ========================

    @Test
    void register_shouldCreateUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("Str0ng!Pass");
        request.setPhoneNumber("9876543210");

        when(userRepository.existsByEmailAndDeletedFalse("john@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("Str0ng!Pass")).thenReturn("encoded-password");

        User savedUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encoded-password")
                .phoneNumber("9876543210")
                .roles(Set.of(studentRole))
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("9876543210", response.getPhoneNumber());

        verify(userRepository).existsByEmailAndDeletedFalse("john@example.com");
        verify(roleRepository).findByName(RoleName.ROLE_STUDENT);
        verify(passwordEncoder).encode("Str0ng!Pass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmailAndDeletedFalse("existing@example.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrowWhenStudentRoleNotFound() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");

        when(userRepository.existsByEmailAndDeletedFalse("new@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_STUDENT)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldNormalizeEmailToLowercase() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("Jane.Doe@Example.COM");
        request.setPassword("Str0ng!Pass");

        when(userRepository.existsByEmailAndDeletedFalse("jane.doe@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return User.builder()
                    .id(2L)
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .email(u.getEmail())
                    .password(u.getPassword())
                    .phoneNumber(u.getPhoneNumber())
                    .roles(u.getRoles())
                    .build();
        });

        UserResponse response = authService.register(request);

        assertEquals("jane.doe@example.com", response.getEmail());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("jane.doe@example.com", captor.getValue().getEmail());
    }

    // ========================
    // Login
    // ========================

    @Test
    void login_shouldReturnLoginResponseOnSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("Str0ng!Pass");

        User user = User.builder()
                .id(1L)
                .email("john@example.com")
                .password("encoded-pass")
                .roles(Set.of(studentRole))
                .build();

        when(userRepository.findByEmailAndDeletedFalse("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("john@example.com", Set.of("ROLE_STUDENT"))).thenReturn("jwt-token");

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900, response.getExpiresIn());
        assertTrue(response.getRoles().contains("ROLE_STUDENT"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmailAndDeletedFalse("john@example.com");
        verify(jwtService).generateToken("john@example.com", Set.of("ROLE_STUDENT"));
    }

    @Test
    void login_shouldThrowBadCredentialsOnInvalidAuth() {
        LoginRequest request = new LoginRequest();
        request.setEmail("bad@example.com");
        request.setPassword("wrong");

        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_shouldThrowWhenUserNotFoundAfterAuth() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@example.com");
        request.setPassword("Str0ng!Pass");

        when(userRepository.findByEmailAndDeletedFalse("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    // ========================
    // Forgot Password
    // ========================

    @Test
    void forgotPassword_shouldGenerateResetToken() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("john@example.com");

        User user = User.builder()
                .id(1L)
                .email("john@example.com")
                .firstName("John")
                .build();

        when(userRepository.findByEmailAndDeletedFalse("john@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.forgotPassword(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertNotNull(saved.getResetToken());
        assertNotNull(saved.getResetTokenExpiry());
        assertTrue(saved.getResetTokenExpiry().isAfter(LocalDateTime.now()));
        verify(emailService).sendPasswordResetEmail(eq("john@example.com"), eq(saved.getResetToken()), eq("John"));
    }

    @Test
    void forgotPassword_shouldThrowWhenUserNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@example.com");

        when(userRepository.findByEmailAndDeletedFalse("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.forgotPassword(request));
    }

    @Test
    void forgotPassword_shouldLogWarningWhenEmailFails() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("john@example.com");

        User user = User.builder()
                .id(1L)
                .email("john@example.com")
                .firstName("John")
                .build();

        when(userRepository.findByEmailAndDeletedFalse("john@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doThrow(new RuntimeException("SMTP failure")).when(emailService)
                .sendPasswordResetEmail(anyString(), anyString(), anyString());

        // Should not throw — the service catches email failures gracefully
        assertDoesNotThrow(() -> authService.forgotPassword(request));
    }

    // ========================
    // Reset Password
    // ========================

    @Test
    void resetPassword_shouldUpdatePasswordWithValidToken() {
        String token = UUID.randomUUID().toString();
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword("NewStr0ng!Pass");

        User user = User.builder()
                .id(1L)
                .email("john@example.com")
                .password("old-encoded")
                .resetToken(token)
                .resetTokenExpiry(LocalDateTime.now().plusHours(1))
                .build();

        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewStr0ng!Pass")).thenReturn("new-encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.resetPassword(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("new-encoded", saved.getPassword());
        assertNull(saved.getResetToken());
        assertNull(saved.getResetTokenExpiry());
    }

    @Test
    void resetPassword_shouldThrowWhenTokenNotFound() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNewPassword("NewStr0ng!Pass");

        when(userRepository.findByResetToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_shouldThrowWhenTokenExpired() {
        String token = UUID.randomUUID().toString();
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword("NewStr0ng!Pass");

        User user = User.builder()
                .id(1L)
                .email("john@example.com")
                .resetToken(token)
                .resetTokenExpiry(LocalDateTime.now().minusHours(1))
                .build();

        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> authService.resetPassword(request));
    }
}
