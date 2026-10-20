package com.sanedge.pointofsale.gateway.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.gateway.dto.inputs.CreateUserInput;
import com.sanedge.pointofsale.gateway.dto.inputs.PageInput;
import com.sanedge.pointofsale.gateway.dto.inputs.UpdateUserInput;
import com.sanedge.pointofsale.gateway.dto.responses.UserResponse;
import com.sanedge.pointofsale.gateway.dto.responses.UserResponseDeleteAt;
import com.sanedge.pointofsale.gateway.mapper.ProtoMapper;
import com.sanedge.pointofsale.pb.user.UserCommandServiceGrpc;
import com.sanedge.pointofsale.pb.user.UserQueryServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserResolver {
    private final UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub;
    private final UserCommandServiceGrpc.UserCommandServiceBlockingStub userCommandStub;

    @QueryMapping
    public ApiResponsePagination<List<UserResponse>> users(@Argument PageInput input) {
        var resp = userQueryStub.findAll(com.sanedge.pointofsale.pb.user.FindAllUserRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<UserResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.users(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    public ApiResponse<UserResponse> user(@Argument Integer id) {
        var resp = userQueryStub.findById(com.sanedge.pointofsale.pb.user.FindByIdUserRequest.newBuilder()
                .setId(id)
                .build());
        return ApiResponse.<UserResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.user(resp.getData()))
                .build();
    }

    @QueryMapping
    public ApiResponsePagination<List<UserResponseDeleteAt>> usersActive(@Argument PageInput input) {
        var resp = userQueryStub.findByActive(com.sanedge.pointofsale.pb.user.FindAllUserRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<UserResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.usersDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    public ApiResponsePagination<List<UserResponseDeleteAt>> usersTrashed(@Argument PageInput input) {
        var resp = userQueryStub.findByTrashed(com.sanedge.pointofsale.pb.user.FindAllUserRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<UserResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.usersDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<UserResponse> createUser(@Argument CreateUserInput input) {
        var resp = userCommandStub.create(com.sanedge.pointofsale.pb.user.CreateUserRequest.newBuilder()
                .setUsername(nvl(input.username()))
                .setFirstname(nvl(input.firstname()))
                .setLastname(nvl(input.lastname()))
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
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<UserResponse> updateUser(@Argument UpdateUserInput input) {
        var resp = userCommandStub.update(com.sanedge.pointofsale.pb.user.UpdateUserRequest.newBuilder()
                .setId(nvl(input.id()))
                .setUsername(nvl(input.username()))
                .setFirstname(nvl(input.firstname()))
                .setLastname(nvl(input.lastname()))
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
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<UserResponseDeleteAt> trashUser(@Argument Integer id) {
        var resp = userCommandStub.trashedUser(com.sanedge.pointofsale.pb.user.FindByIdUserRequest.newBuilder()
                .setId(id)
                .build());
        return ApiResponse.<UserResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.userDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<UserResponseDeleteAt> restoreUser(@Argument Integer id) {
        var resp = userCommandStub.restoreUser(com.sanedge.pointofsale.pb.user.FindByIdUserRequest.newBuilder()
                .setId(id)
                .build());
        return ApiResponse.<UserResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.userDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteUserPermanent(@Argument Integer id) {
        var resp = userCommandStub.deleteUserPermanent(com.sanedge.pointofsale.pb.user.FindByIdUserRequest.newBuilder()
                .setId(id)
                .build());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> restoreAllUsers() {
        var resp = userCommandStub.restoreAllUser(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteAllUsersPermanent() {
        var resp = userCommandStub.deleteAllUserPermanent(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    private static int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
