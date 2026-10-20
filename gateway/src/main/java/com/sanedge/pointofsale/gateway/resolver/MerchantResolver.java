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
import com.sanedge.pointofsale.gateway.dto.inputs.CreateMerchantInput;
import com.sanedge.pointofsale.gateway.dto.inputs.PageInput;
import com.sanedge.pointofsale.gateway.dto.inputs.UpdateMerchantInput;
import com.sanedge.pointofsale.gateway.dto.responses.MerchantResponse;
import com.sanedge.pointofsale.gateway.dto.responses.MerchantResponseDeleteAt;
import com.sanedge.pointofsale.gateway.mapper.ProtoMapper;
import com.sanedge.pointofsale.pb.merchant.MerchantCommandServiceGrpc;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MerchantResolver {
    private final MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub;
    private final MerchantCommandServiceGrpc.MerchantCommandServiceBlockingStub merchantCommandStub;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<MerchantResponse>> merchants(@Argument PageInput input) {
        var resp = merchantQueryStub.findAll(com.sanedge.pointofsale.pb.merchant.FindAllMerchantRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<MerchantResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchants(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<MerchantResponseDeleteAt>> merchantsActive(@Argument PageInput input) {
        var resp = merchantQueryStub.findByActive(com.sanedge.pointofsale.pb.merchant.FindAllMerchantRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<MerchantResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<MerchantResponseDeleteAt>> merchantsTrashed(@Argument PageInput input) {
        var resp = merchantQueryStub.findByTrashed(com.sanedge.pointofsale.pb.merchant.FindAllMerchantRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<MerchantResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<MerchantResponse> merchant(@Argument Integer id) {
        var resp = merchantQueryStub.findById(com.sanedge.pointofsale.pb.merchant.FindByIdMerchantRequest.newBuilder()
                .setMerchantId(id)
                .build());
        return ApiResponse.<MerchantResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchant(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<MerchantResponse> createMerchant(@Argument CreateMerchantInput input) {
        var resp = merchantCommandStub.createMerchant(
                com.sanedge.pointofsale.pb.merchant.CreateMerchantRequest.newBuilder()
                        .setUserId(nvl(input.userId()))
                        .setName(nvl(input.name()))
                        .setDescription(nvl(input.description()))
                        .setAddress(nvl(input.address()))
                        .setContactEmail(nvl(input.contactEmail()))
                        .setContactPhone(nvl(input.contactPhone()))
                        .setStatus(nvl(input.status()))
                        .build());
        return ApiResponse.<MerchantResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchant(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<MerchantResponse> updateMerchant(@Argument UpdateMerchantInput input) {
        var resp = merchantCommandStub.updateMerchant(
                com.sanedge.pointofsale.pb.merchant.UpdateMerchantRequest.newBuilder()
                        .setMerchantId(nvl(input.merchantId()))
                        .setUserId(nvl(input.userId()))
                        .setName(nvl(input.name()))
                        .setDescription(nvl(input.description()))
                        .setAddress(nvl(input.address()))
                        .setContactEmail(nvl(input.contactEmail()))
                        .setContactPhone(nvl(input.contactPhone()))
                        .setStatus(nvl(input.status()))
                        .build());
        return ApiResponse.<MerchantResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchant(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<MerchantResponseDeleteAt> trashedMerchant(@Argument Integer id) {
        var resp = merchantCommandStub.trashedMerchant(
                com.sanedge.pointofsale.pb.merchant.FindByIdMerchantRequest.newBuilder()
                        .setMerchantId(id)
                        .build());
        return ApiResponse.<MerchantResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<MerchantResponseDeleteAt> restoreMerchant(@Argument Integer id) {
        var resp = merchantCommandStub.restoreMerchant(
                com.sanedge.pointofsale.pb.merchant.FindByIdMerchantRequest.newBuilder()
                        .setMerchantId(id)
                        .build());
        return ApiResponse.<MerchantResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.merchantDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteMerchantPermanent(@Argument Integer id) {
        var resp = merchantCommandStub.deleteMerchantPermanent(
                com.sanedge.pointofsale.pb.merchant.FindByIdMerchantRequest.newBuilder()
                        .setMerchantId(id)
                        .build());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> restoreAllMerchants() {
        var resp = merchantCommandStub.restoreAllMerchant(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteAllMerchantsPermanent() {
        var resp = merchantCommandStub.deleteAllMerchantPermanent(Empty.getDefaultInstance());
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
