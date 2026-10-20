package com.sanedge.pointofsale.merchant.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.merchant.ApiMerchantsResponse;
import com.sanedge.pointofsale.pb.merchant.ApiResponseMerchant;
import com.sanedge.pointofsale.pb.merchant.ApiResponsePaginationMerchant;
import com.sanedge.pointofsale.pb.merchant.ApiResponsePaginationMerchantDeleteAt;
import com.sanedge.pointofsale.pb.merchant.FindAllMerchantRequest;
import com.sanedge.pointofsale.pb.merchant.FindByIdMerchantRequest;
import com.sanedge.pointofsale.pb.merchant.FindByUserIdMerchantRequest;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.pointofsale.pb.merchant.MerchantResponse;
import com.sanedge.pointofsale.pb.merchant.MerchantResponseDeleteAt;
import com.sanedge.pointofsale.merchant.service.MerchantQueryService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantQueryHandler extends MerchantQueryServiceGrpc.MerchantQueryServiceImplBase {

    private final MerchantQueryService merchantQueryService;

    @Override
    public void findAll(FindAllMerchantRequest request, StreamObserver<ApiResponsePaginationMerchant> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse>> res = merchantQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationMerchant.Builder builder = ApiResponsePaginationMerchant.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(m -> builder.addData(toProto(m)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findById(FindByIdMerchantRequest request, StreamObserver<ApiResponseMerchant> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse> res = merchantQueryService
                    .findById(request.getMerchantId());

            observer.onNext(ApiResponseMerchant.newBuilder()
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
    public void findByUserId(FindByUserIdMerchantRequest request, StreamObserver<ApiMerchantsResponse> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse>> res = merchantQueryService
                    .findByUserId(request.getUserId());

            ApiMerchantsResponse.Builder builder = ApiMerchantsResponse.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(m -> builder.addData(toProto(m)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByActive(FindAllMerchantRequest request,
            StreamObserver<ApiResponsePaginationMerchantDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponseDeleteAt>> res = merchantQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationMerchantDeleteAt.Builder builder = ApiResponsePaginationMerchantDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(m -> builder.addData(toProtoDeleteAt(m)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashed(FindAllMerchantRequest request,
            StreamObserver<ApiResponsePaginationMerchantDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponseDeleteAt>> res = merchantQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationMerchantDeleteAt.Builder builder = ApiResponsePaginationMerchantDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(m -> builder.addData(toProtoDeleteAt(m)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchants toFindAll(
            FindAllMerchantRequest request) {
        com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchants req = new com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchants();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private MerchantResponse toProto(com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse m) {
        return MerchantResponse.newBuilder()
                .setMerchantId(m.getId() == null ? 0 : m.getId().intValue())
                .setUserId(m.getUserId() == null ? 0 : m.getUserId())
                .setName(ProtoMappers.nvl(m.getName()))
                .setDescription(ProtoMappers.nvl(m.getDescription()))
                .setAddress(ProtoMappers.nvl(m.getAddress()))
                .setContactEmail(ProtoMappers.nvl(m.getContactEmail()))
                .setContactPhone(ProtoMappers.nvl(m.getContactPhone()))
                .setStatus(ProtoMappers.nvl(m.getStatus()))
                .setCreatedAt(ProtoMappers.nvl(m.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(m.getUpdatedAt()))
                .build();
    }

    private MerchantResponseDeleteAt toProtoDeleteAt(
            com.sanedge.pointofsale.merchant.domain.responses.MerchantResponseDeleteAt m) {
        return MerchantResponseDeleteAt.newBuilder()
                .setMerchantId(m.getId() == null ? 0 : m.getId().intValue())
                .setUserId(m.getUserId() == null ? 0 : m.getUserId())
                .setName(ProtoMappers.nvl(m.getName()))
                .setDescription(ProtoMappers.nvl(m.getDescription()))
                .setAddress(ProtoMappers.nvl(m.getAddress()))
                .setContactEmail(ProtoMappers.nvl(m.getContactEmail()))
                .setContactPhone(ProtoMappers.nvl(m.getContactPhone()))
                .setStatus(ProtoMappers.nvl(m.getStatus()))
                .setCreatedAt(ProtoMappers.nvl(m.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(m.getUpdatedAt()))
                .setDeletedAt(ProtoMappers.nvl(m.getDeletedAt()))
                .build();
    }
}
