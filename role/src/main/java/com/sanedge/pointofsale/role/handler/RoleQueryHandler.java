package com.sanedge.pointofsale.role.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.role.ApiResponsePaginationRole;
import com.sanedge.pointofsale.pb.role.ApiResponsePaginationRoleDeleteAt;
import com.sanedge.pointofsale.pb.role.ApiResponseRole;
import com.sanedge.pointofsale.pb.role.ApiRolesResponse;
import com.sanedge.pointofsale.pb.role.FindAllRoleRequest;
import com.sanedge.pointofsale.pb.role.FindByIdRoleRequest;
import com.sanedge.pointofsale.pb.role.FindByNameRoleRequest;
import com.sanedge.pointofsale.pb.role.FindByUserIdRoleRequest;
import com.sanedge.pointofsale.pb.role.RoleQueryServiceGrpc;
import com.sanedge.pointofsale.pb.role.RoleResponse;
import com.sanedge.pointofsale.pb.role.RoleResponseDeleteAt;
import com.sanedge.pointofsale.role.domain.requests.FindAllRoles;
import com.sanedge.pointofsale.role.service.RoleQueryService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleQueryHandler extends RoleQueryServiceGrpc.RoleQueryServiceImplBase {

    private final RoleQueryService roleQueryService;

    @Override
    public void findAll(FindAllRoleRequest request, StreamObserver<ApiResponsePaginationRole> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.role.domain.responses.RoleResponse>> res = roleQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationRole.Builder builder = ApiResponsePaginationRole.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findById(FindByIdRoleRequest request, StreamObserver<ApiResponseRole> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.role.domain.responses.RoleResponse> res = roleQueryService
                    .findById(request.getRoleId());

            observer.onNext(ApiResponseRole.newBuilder()
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
    public void findByName(FindByNameRoleRequest request, StreamObserver<ApiResponseRole> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.role.domain.responses.RoleResponse> res = roleQueryService
                    .findByName(request.getName());

            observer.onNext(ApiResponseRole.newBuilder()
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
    public void findByUserId(FindByUserIdRoleRequest request, StreamObserver<ApiRolesResponse> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.role.domain.responses.RoleResponse>> res = roleQueryService
                    .findByUserId(request.getUserId());

            ApiRolesResponse.Builder builder = ApiRolesResponse.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(r -> builder.addData(toProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByActive(FindAllRoleRequest request, StreamObserver<ApiResponsePaginationRoleDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.role.domain.responses.RoleResponseDeleteAt>> res = roleQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationRoleDeleteAt.Builder builder = ApiResponsePaginationRoleDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashed(FindAllRoleRequest request, StreamObserver<ApiResponsePaginationRoleDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.role.domain.responses.RoleResponseDeleteAt>> res = roleQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationRoleDeleteAt.Builder builder = ApiResponsePaginationRoleDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private FindAllRoles toFindAll(FindAllRoleRequest request) {
        FindAllRoles req = new FindAllRoles();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private RoleResponse toProto(com.sanedge.pointofsale.role.domain.responses.RoleResponse r) {
        return RoleResponse.newBuilder()
                .setRoleId(r.getId() == null ? 0 : r.getId())
                .setName(ProtoMappers.nvl(r.getName()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private RoleResponseDeleteAt toProtoDeleteAt(
            com.sanedge.pointofsale.role.domain.responses.RoleResponseDeleteAt r) {
        return RoleResponseDeleteAt.newBuilder()
                .setRoleId(r.getId() == null ? 0 : r.getId())
                .setName(ProtoMappers.nvl(r.getName()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(ProtoMappers.nvl(r.getDeletedAt()))
                .build();
    }
}
