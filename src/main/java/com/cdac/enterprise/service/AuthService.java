package com.cdac.enterprise.service;

import com.cdac.enterprise.dto.ForgotPasswordRequest;
import com.cdac.enterprise.dto.LoginRequest;
import com.cdac.enterprise.dto.LoginResponse;
import com.cdac.enterprise.dto.RegisterRequest;
import com.cdac.enterprise.dto.ResetPasswordRequest;
import com.cdac.enterprise.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}