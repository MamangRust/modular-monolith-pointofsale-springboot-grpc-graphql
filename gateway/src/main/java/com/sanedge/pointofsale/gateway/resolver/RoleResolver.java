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
import com.sanedge.pointofsale.gateway.dto.inputs.CreateRoleInput;
import com.sanedge.pointofsale.gateway.dto.inputs.PageInput;
import com.sanedge.pointofsale.gateway.dto.inputs.UpdateRoleInput;
import com.sanedge.pointofsale.gateway.dto.responses.RoleResponse;
import com.sanedge.pointofsale.gateway.dto.responses.RoleResponseDeleteAt;
import com.sanedge.pointofsale.gateway.mapper.ProtoMapper;
import com.sanedge.pointofsale.pb.role.RoleCommandServiceGrpc;
import com.sanedge.pointofsale.pb.role.RoleQueryServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RoleResolver {
    private final RoleQueryServiceGrpc.RoleQueryServiceBlockingStub roleQueryStub;
    private final RoleCommandServiceGrpc.RoleCommandServiceBlockingStub roleCommandStub;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<RoleResponse>> roles(@Argument PageInput input) {
        var resp = roleQueryStub.findAll(com.sanedge.pointofsale.pb.role.FindAllRoleRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<RoleResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.roles(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<RoleResponseDeleteAt>> rolesActive(@Argument PageInput input) {
        var resp = roleQueryStub.findByActive(com.sanedge.pointofsale.pb.role.FindAllRoleRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<RoleResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.rolesDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<RoleResponseDeleteAt>> rolesTrashed(@Argument PageInput input) {
        var resp = roleQueryStub.findByTrashed(com.sanedge.pointofsale.pb.role.FindAllRoleRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<RoleResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.rolesDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<RoleResponse> role(@Argument Integer id) {
        var resp = roleQueryStub.findById(com.sanedge.pointofsale.pb.role.FindByIdRoleRequest.newBuilder()
                .setRoleId(id)
                .build());
        return ApiResponse.<RoleResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.role(resp.getData()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<RoleResponse>> rolesByUserId(@Argument Integer userId) {
        var resp = roleQueryStub.findByUserId(com.sanedge.pointofsale.pb.role.FindByUserIdRoleRequest.newBuilder()
                .setUserId(userId)
                .build());
        return ApiResponse.<List<RoleResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.roles(resp.getDataList()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<RoleResponse> roleByName(@Argument String name) {
        var resp = roleQueryStub.findByName(com.sanedge.pointofsale.pb.role.FindByNameRoleRequest.newBuilder()
                .setName(name)
                .build());
        return ApiResponse.<RoleResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.role(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<RoleResponse> createRole(@Argument CreateRoleInput input) {
        var resp = roleCommandStub.createRole(com.sanedge.pointofsale.pb.role.CreateRoleRequest.newBuilder()
                .setName(nvl(input.name()))
                .build());
        return ApiResponse.<RoleResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.role(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<RoleResponse> updateRole(@Argument UpdateRoleInput input) {
        var resp = roleCommandStub.updateRole(com.sanedge.pointofsale.pb.role.UpdateRoleRequest.newBuilder()
                .setRoleId(nvl(input.id()))
                .setName(nvl(input.name()))
                .build());
        return ApiResponse.<RoleResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.role(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<RoleResponseDeleteAt> trashRole(@Argument Integer id) {
        var resp = roleCommandStub.trashedRole(com.sanedge.pointofsale.pb.role.FindByIdRoleRequest.newBuilder()
                .setRoleId(id)
                .build());
        return ApiResponse.<RoleResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.roleDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<RoleResponseDeleteAt> restoreRole(@Argument Integer id) {
        var resp = roleCommandStub.restoreRole(com.sanedge.pointofsale.pb.role.FindByIdRoleRequest.newBuilder()
                .setRoleId(id)
                .build());
        return ApiResponse.<RoleResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.roleDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteRolePermanent(@Argument Integer id) {
        var resp = roleCommandStub.deleteRolePermanent(com.sanedge.pointofsale.pb.role.FindByIdRoleRequest.newBuilder()
                .setRoleId(id)
                .build());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> restoreAllRoles() {
        var resp = roleCommandStub.restoreAllRole(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteAllRolesPermanent() {
        var resp = roleCommandStub.deleteAllRolePermanent(Empty.getDefaultInstance());
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
