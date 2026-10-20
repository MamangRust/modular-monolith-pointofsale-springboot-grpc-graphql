package com.sanedge.pointofsale.auth.service;

import com.sanedge.pointofsale.auth.domain.requests.AuthRequest;
import com.sanedge.pointofsale.auth.domain.requests.RegisterRequest;
import com.sanedge.pointofsale.auth.domain.requests.ResetPasswordRequest;
import com.sanedge.pointofsale.auth.domain.responses.TokenResponse;
import com.sanedge.pointofsale.auth.domain.responses.UserResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;

public interface AuthService {
    ApiResponse<TokenResponse> login(AuthRequest loginRequest);

    ApiResponse<UserResponse> register(RegisterRequest registerRequest);

    ApiResponse<TokenResponse> refreshToken(String refreshToken);

    ApiResponse<UserResponse> getCurrentUser(String username);

    /** Issues a reset token and enqueues the reset email. */
    ApiResponse<Void> forgotPassword(String email);

    /** Consumes a reset token and sets the new password. */
    ApiResponse<Void> resetPassword(ResetPasswordRequest resetPasswordRequest);

    /** Validates and consumes an email verification code; enqueues the confirmation. */
    ApiResponse<Void> verifyCode(String code);
}
