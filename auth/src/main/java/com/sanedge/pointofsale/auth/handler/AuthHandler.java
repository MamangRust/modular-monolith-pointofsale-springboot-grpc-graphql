package com.sanedge.pointofsale.auth.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.auth.service.AuthService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.auth.ApiResponseForgotPassword;
import com.sanedge.pointofsale.pb.auth.ApiResponseGetMe;
import com.sanedge.pointofsale.pb.auth.ApiResponseHealth;
import com.sanedge.pointofsale.pb.auth.ApiResponseLogin;
import com.sanedge.pointofsale.pb.auth.ApiResponseRefreshToken;
import com.sanedge.pointofsale.pb.auth.ApiResponseRegister;
import com.sanedge.pointofsale.pb.auth.ApiResponseResetPassword;
import com.sanedge.pointofsale.pb.auth.ApiResponseVerifyCode;
import com.sanedge.pointofsale.pb.auth.AuthServiceGrpc;
import com.sanedge.pointofsale.pb.auth.ForgotPasswordRequest;
import com.sanedge.pointofsale.pb.auth.GetMeRequest;
import com.sanedge.pointofsale.pb.auth.LoginRequest;
import com.sanedge.pointofsale.pb.auth.RegisterRequest;
import com.sanedge.pointofsale.pb.auth.RefreshTokenRequest;
import com.sanedge.pointofsale.pb.auth.ResetPasswordRequest;
import com.sanedge.pointofsale.pb.auth.TokenResponse;
import com.sanedge.pointofsale.pb.auth.VerifyCodeRequest;
import com.sanedge.pointofsale.pb.user.UserResponse;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthHandler extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;

    @Override
    public void registerUser(RegisterRequest request, StreamObserver<ApiResponseRegister> observer) {
        try {
            com.sanedge.pointofsale.auth.domain.requests.RegisterRequest req = new com.sanedge.pointofsale.auth.domain.requests.RegisterRequest();
            req.setFirstname(request.getFirstname());
            req.setLastname(request.getLastname());
            req.setUsername(request.getUsername());
            req.setEmail(request.getEmail());
            req.setPassword(request.getPassword());
            req.setConfirmPassword(request.getConfirmPassword());

            ApiResponse<com.sanedge.pointofsale.auth.domain.responses.UserResponse> res = authService.register(req);

            ApiResponseRegister.Builder builder = ApiResponseRegister.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProto(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void loginUser(LoginRequest request, StreamObserver<ApiResponseLogin> observer) {
        try {
            com.sanedge.pointofsale.auth.domain.requests.AuthRequest req = new com.sanedge.pointofsale.auth.domain.requests.AuthRequest();
            req.setUsername(request.getUsername());
            req.setPassword(request.getPassword());

            ApiResponse<com.sanedge.pointofsale.auth.domain.responses.TokenResponse> res = authService.login(req);

            ApiResponseLogin.Builder builder = ApiResponseLogin.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProtoToken(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void refreshToken(RefreshTokenRequest request, StreamObserver<ApiResponseRefreshToken> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.auth.domain.responses.TokenResponse> res = authService
                    .refreshToken(request.getRefreshToken());

            ApiResponseRefreshToken.Builder builder = ApiResponseRefreshToken.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProtoToken(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void getMe(GetMeRequest request, StreamObserver<ApiResponseGetMe> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.auth.domain.responses.UserResponse> res = authService
                    .getCurrentUser(request.getUsername());

            ApiResponseGetMe.Builder builder = ApiResponseGetMe.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProto(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void healthCheck(Empty request, StreamObserver<ApiResponseHealth> observer) {
        try {
            observer.onNext(ApiResponseHealth.newBuilder()
                    .setStatus("success")
                    .setMessage("Auth service is healthy")
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void verifyCode(VerifyCodeRequest request, StreamObserver<ApiResponseVerifyCode> observer) {
        try {
            ApiResponse<Void> res = authService.verifyCode(request.getCode());

            observer.onNext(ApiResponseVerifyCode.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request, StreamObserver<ApiResponseForgotPassword> observer) {
        try {
            ApiResponse<Void> res = authService.forgotPassword(request.getEmail());

            observer.onNext(ApiResponseForgotPassword.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, StreamObserver<ApiResponseResetPassword> observer) {
        try {
            com.sanedge.pointofsale.auth.domain.requests.ResetPasswordRequest req = new com.sanedge.pointofsale.auth.domain.requests.ResetPasswordRequest();
            req.setToken(request.getResetToken());
            req.setPassword(request.getPassword());
            req.setConfirmPassword(request.getConfirmPassword());

            ApiResponse<Void> res = authService.resetPassword(req);

            observer.onNext(ApiResponseResetPassword.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private UserResponse toProto(com.sanedge.pointofsale.auth.domain.responses.UserResponse u) {
        return UserResponse.newBuilder()
                .setId(u.getId() == null ? 0 : u.getId())
                .setUsername(ProtoMappers.nvl(u.getUsername()))
                .setFirstname(ProtoMappers.nvl(u.getFirstname()))
                .setLastname(ProtoMappers.nvl(u.getLastname()))
                .setEmail(ProtoMappers.nvl(u.getEmail()))
                .setCreatedAt(ProtoMappers.nvl(u.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(u.getUpdatedAt()))
                .build();
    }

    private TokenResponse toProtoToken(com.sanedge.pointofsale.auth.domain.responses.TokenResponse t) {
        return TokenResponse.newBuilder()
                .setAccessToken(ProtoMappers.nvl(t.getAccess_token()))
                .setRefreshToken(ProtoMappers.nvl(t.getRefresh_token()))
                .build();
    }
}
