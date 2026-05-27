package com.cdac.enterprise.service.impl;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.constant.RoleName;
import com.cdac.enterprise.dto.ForgotPasswordRequest;
import com.cdac.enterprise.dto.LoginRequest;
import com.cdac.enterprise.dto.LoginResponse;
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
import com.cdac.enterprise.service.AuthService;
import com.cdac.enterprise.service.EmailService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final String TOKEN_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase();

        if (userRepository.existsByEmailAndDeletedFalse(normalizedEmail)) {
            throw new ResourceAlreadyExistsException(AppMessages.EMAIL_ALREADY_REGISTERED);
        }

        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new IllegalStateException(AppMessages.DEFAULT_STUDENT_ROLE_NOT_CONFIGURED));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .roles(Set.of(studentRole))
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .build();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase();

        User user = userRepository.findByEmailAndDeletedFalse(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.USER_NOT_FOUND_WITH_EMAIL));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        try {
            emailService.sendPasswordResetEmail(normalizedEmail, resetToken, user.getFirstName());
        } catch (Exception e) {
            // Email not configured (demo mode) — log the token instead of failing
            log.warn("Failed to send password reset email to {}. SMTP not configured.", normalizedEmail);
            log.warn("Password reset token for {}: {}", normalizedEmail, resetToken);
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.INVALID_OR_EXPIRED_RESET_TOKEN));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException(AppMessages.INVALID_OR_EXPIRED_RESET_TOKEN);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException(AppMessages.INVALID_EMAIL_OR_PASSWORD);
        }

        User user = userRepository.findByEmailAndDeletedFalse(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException(AppMessages.INVALID_EMAIL_OR_PASSWORD));

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        String accessToken = jwtService.generateToken(user.getEmail(), roles);

        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .accessToken(accessToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(ACCESS_TOKEN_EXPIRES_IN_SECONDS)
                .build();
    }
}