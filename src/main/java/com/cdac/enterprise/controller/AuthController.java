package com.cdac.enterprise.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.enterprise.constant.ApiPaths;
import com.cdac.enterprise.constant.AppMessages;
import com.cdac.enterprise.dto.ApiResponse;
import com.cdac.enterprise.dto.ForgotPasswordRequest;
import com.cdac.enterprise.dto.RegisterRequest;
import com.cdac.enterprise.dto.ResetPasswordRequest;
import com.cdac.enterprise.dto.UserResponse;
import com.cdac.enterprise.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.cdac.enterprise.dto.LoginRequest;
import com.cdac.enterprise.dto.LoginResponse;

@Tag(name = "Authentication", description = "APIs for user registration, login, and authentication workflows")
@RestController
@RequestMapping(ApiPaths.AUTH)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register a new student user",
            description = "Creates a new student account, hashes the password using BCrypt, and assigns ROLE_STUDENT"
    )

    @PostMapping(ApiPaths.REGISTER)
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
    	
        UserResponse userResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        AppMessages.USER_REGISTERED_SUCCESSFULLY,
                        userResponse
                ));
    }
    
    @Operation(
            summary = "Login user",
            description = "Authenticates user credentials using Spring Security AuthenticationManager"
    )
    
    @PostMapping(ApiPaths.LOGIN)
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse loginResponse = authService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.LOGIN_SUCCESSFUL,
                        loginResponse
                ));
    }

    @Operation(
            summary = "Forgot password",
            description = "Generates a reset token and sends it via email to the user"
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.forgotPassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.RESET_TOKEN_SENT,
                        null
                ));
    }

    @Operation(
            summary = "Reset password",
            description = "Resets the password using a valid reset token"
    )
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        AppMessages.PASSWORD_RESET_SUCCESSFULLY,
                        null
                ));
    }
}