package com.sanedge.pointofsale.user.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.user.ApiResponseUser;
import com.sanedge.pointofsale.pb.user.ApiResponseUserAll;
import com.sanedge.pointofsale.pb.user.ApiResponseUserDeleteAt;
import com.sanedge.pointofsale.pb.user.CreateUserRequest;
import com.sanedge.pointofsale.pb.user.FindByIdUserRequest;
import com.sanedge.pointofsale.pb.user.UpdateUserRequest;
import com.sanedge.pointofsale.pb.user.UserCommandServiceGrpc;
import com.sanedge.pointofsale.pb.user.UserResponse;
import com.sanedge.pointofsale.pb.user.UserResponseDeleteAt;
import com.sanedge.pointofsale.pb.user.VerifyPasswordRequest;
import com.sanedge.pointofsale.pb.user.VerifyPasswordResponse;
import com.sanedge.pointofsale.user.service.UserCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCommandHandler extends UserCommandServiceGrpc.UserCommandServiceImplBase {

    private final UserCommandService userCommandService;

    @Override
    public void create(CreateUserRequest request, StreamObserver<ApiResponseUser> observer) {
        try {
            com.sanedge.pointofsale.user.domain.requests.CreateUserRequest req = new com.sanedge.pointofsale.user.domain.requests.CreateUserRequest();
            req.setUsername(request.getUsername());
            req.setFirstname(request.getFirstname());
            req.setLastname(request.getLastname());
            req.setEmail(request.getEmail());
            req.setPassword(request.getPassword());
            req.setConfirmPassword(request.getConfirmPassword());

            ApiResponse<com.sanedge.pointofsale.user.domain.responses.UserResponse> res = userCommandService
                    .create(req);

            observer.onNext(ApiResponseUser.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setData(toProto(res.getData()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void update(UpdateUserRequest request, StreamObserver<ApiResponseUser> observer) {
        try {
            com.sanedge.pointofsale.user.domain.requests.UpdateUserRequest req = new com.sanedge.pointofsale.user.domain.requests.UpdateUserRequest();
            req.setId(request.getId());
            req.setUsername(request.getUsername());
            req.setFirstname(request.getFirstname());
            req.setLastname(request.getLastname());
            req.setEmail(request.getEmail());
            req.setPassword(request.getPassword());
            req.setConfirmPassword(request.getConfirmPassword());

            ApiResponse<com.sanedge.pointofsale.user.domain.responses.UserResponse> res = userCommandService
                    .update(req);

            observer.onNext(ApiResponseUser.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setData(toProto(res.getData()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void trashedUser(FindByIdUserRequest request, StreamObserver<ApiResponseUserDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.user.domain.responses.UserResponseDeleteAt> res = userCommandService
                    .trashed(request.getId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreUser(FindByIdUserRequest request, StreamObserver<ApiResponseUserDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.user.domain.responses.UserResponseDeleteAt> res = userCommandService
                    .restore(request.getId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteUserPermanent(FindByIdUserRequest request, StreamObserver<ApiResponseUserAll> observer) {
        try {
            ApiResponse<Boolean> res = userCommandService.deletePermanent(request.getId());
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllUser(Empty request, StreamObserver<ApiResponseUserAll> observer) {
        try {
            ApiResponse<Boolean> res = userCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllUserPermanent(Empty request, StreamObserver<ApiResponseUserAll> observer) {
        try {
            ApiResponse<Boolean> res = userCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void verifyPassword(VerifyPasswordRequest request, StreamObserver<VerifyPasswordResponse> observer) {
        try {
            com.sanedge.pointofsale.user.domain.responses.VerifyPasswordResponse res = userCommandService
                    .verifyPassword(request.getUsername(), request.getPassword());

            observer.onNext(VerifyPasswordResponse.newBuilder()
                    .setValid(res.isValid())
                    .setUserId(res.getUserId() == null ? 0 : res.getUserId())
                    .setUsername(ProtoMappers.nvl(res.getUsername()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private UserResponse toProto(com.sanedge.pointofsale.user.domain.responses.UserResponse u) {
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

    private ApiResponseUserDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.pointofsale.user.domain.responses.UserResponseDeleteAt> res) {
        UserResponseDeleteAt data = UserResponseDeleteAt.newBuilder()
                .setId(res.getData().getId() == null ? 0 : res.getData().getId())
                .setFirstname(ProtoMappers.nvl(res.getData().getFirstname()))
                .setLastname(ProtoMappers.nvl(res.getData().getLastname()))
                .setEmail(ProtoMappers.nvl(res.getData().getEmail()))
                .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                .setDeletedAt(ProtoMappers.nvl(res.getData().getDeletedAt()))
                .build();
        return ApiResponseUserDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(data)
                .build();
    }

    private ApiResponseUserAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseUserAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setSuccess(Boolean.TRUE.equals(res.getData()))
                .build();
    }
}
