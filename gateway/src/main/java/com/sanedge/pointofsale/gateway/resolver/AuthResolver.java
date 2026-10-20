package com.sanedge.pointofsale.gateway.resolver;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.gateway.dto.inputs.LoginInput;
import com.sanedge.pointofsale.gateway.dto.inputs.RefreshTokenInput;
import com.sanedge.pointofsale.gateway.dto.inputs.RegisterInput;
import com.sanedge.pointofsale.gateway.dto.responses.TokenResponse;
import com.sanedge.pointofsale.gateway.dto.responses.UserResponse;
import com.sanedge.pointofsale.gateway.mapper.ProtoMapper;
import com.sanedge.pointofsale.pb.auth.AuthServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthServiceGrpc.AuthServiceBlockingStub authStub;

    @MutationMapping
    public ApiResponse<UserResponse> register(@Argument RegisterInput input) {
        var resp = authStub.registerUser(com.sanedge.pointofsale.pb.auth.RegisterRequest.newBuilder()
                .setFirstname(nvl(input.firstname()))
                .setLastname(nvl(input.lastname()))
                .setUsername(nvl(input.username()))
                .setEmail(nvl(input.email()))
                .setPassword(nvl(input.password()))
                .setConfirmPassword(nvl(input.confirmPassword()))
                .build());
        return ApiResponse.<UserResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.user(resp.getData()))
                .build();
    }

    @MutationMapping
    public ApiResponse<TokenResponse> login(@Argument LoginInput input) {
        var resp = authStub.loginUser(com.sanedge.pointofsale.pb.auth.LoginRequest.newBuilder()
                .setUsername(nvl(input.username()))
                .setPassword(nvl(input.password()))
                .build());
        return ApiResponse.<TokenResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.token(resp.getData()))
                .build();
    }

    @MutationMapping
    public ApiResponse<TokenResponse> refreshToken(@Argument RefreshTokenInput input) {
        var resp = authStub.refreshToken(com.sanedge.pointofsale.pb.auth.RefreshTokenRequest.newBuilder()
                .setRefreshToken(nvl(input.refreshToken()))
                .build());
        return ApiResponse.<TokenResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.token(resp.getData()))
                .build();
    }

    @QueryMapping
    public String healthchecker() {
        return "✅ API is up and running!";
    }

    @QueryMapping
    public ApiResponse<UserResponse> me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var resp = authStub.getMe(com.sanedge.pointofsale.pb.auth.GetMeRequest.newBuilder()
                .setUsername(username)
                .build());
        return ApiResponse.<UserResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.user(resp.getData()))
                .build();
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
