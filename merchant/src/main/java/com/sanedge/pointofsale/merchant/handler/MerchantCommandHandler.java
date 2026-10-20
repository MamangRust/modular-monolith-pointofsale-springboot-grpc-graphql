package com.sanedge.pointofsale.merchant.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.merchant.ApiResponseMerchant;
import com.sanedge.pointofsale.pb.merchant.ApiResponseMerchantAll;
import com.sanedge.pointofsale.pb.merchant.ApiResponseMerchantDeleteAt;
import com.sanedge.pointofsale.pb.merchant.CreateMerchantRequest;
import com.sanedge.pointofsale.pb.merchant.FindByIdMerchantRequest;
import com.sanedge.pointofsale.pb.merchant.MerchantCommandServiceGrpc;
import com.sanedge.pointofsale.pb.merchant.MerchantResponse;
import com.sanedge.pointofsale.pb.merchant.MerchantResponseDeleteAt;
import com.sanedge.pointofsale.pb.merchant.UpdateMerchantRequest;
import com.sanedge.pointofsale.pb.merchant.UpdateMerchantStatusRequest;
import com.sanedge.pointofsale.merchant.service.MerchantCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantCommandHandler extends MerchantCommandServiceGrpc.MerchantCommandServiceImplBase {

    private final MerchantCommandService merchantCommandService;

    @Override
    public void createMerchant(CreateMerchantRequest request, StreamObserver<ApiResponseMerchant> observer) {
        try {
            com.sanedge.pointofsale.merchant.domain.requests.CreateMerchantRequest req = new com.sanedge.pointofsale.merchant.domain.requests.CreateMerchantRequest();
            req.setUserId(request.getUserId());
            req.setName(request.getName());
            req.setDescription(request.getDescription());
            req.setAddress(request.getAddress());
            req.setContactEmail(request.getContactEmail());
            req.setContactPhone(request.getContactPhone());
            req.setStatus(request.getStatus());

            ApiResponse<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse> res = merchantCommandService
                    .createMerchant(req);

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
    public void updateMerchant(UpdateMerchantRequest request, StreamObserver<ApiResponseMerchant> observer) {
        try {
            com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantRequest req = new com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setUserId(request.getUserId());
            req.setName(request.getName());
            req.setDescription(request.getDescription());
            req.setAddress(request.getAddress());
            req.setContactEmail(request.getContactEmail());
            req.setContactPhone(request.getContactPhone());
            req.setStatus(request.getStatus());

            ApiResponse<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse> res = merchantCommandService
                    .updateMerchant(req);

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
    public void updateMerchantStatus(UpdateMerchantStatusRequest request,
            StreamObserver<ApiResponseMerchant> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse> res = merchantCommandService
                    .updateMerchantStatus(request.getMerchantId(), request.getStatus());

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
    public void trashedMerchant(FindByIdMerchantRequest request,
            StreamObserver<ApiResponseMerchantDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponseDeleteAt> res = merchantCommandService
                    .trashedMerchant(request.getMerchantId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreMerchant(FindByIdMerchantRequest request,
            StreamObserver<ApiResponseMerchantDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponseDeleteAt> res = merchantCommandService
                    .restoreMerchant(request.getMerchantId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteMerchantPermanent(FindByIdMerchantRequest request,
            StreamObserver<ApiResponseMerchantAll> observer) {
        try {
            ApiResponse<Boolean> res = merchantCommandService.deleteMerchantPermanent(request.getMerchantId());
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllMerchant(Empty request, StreamObserver<ApiResponseMerchantAll> observer) {
        try {
            ApiResponse<Boolean> res = merchantCommandService.restoreAllMerchant();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllMerchantPermanent(Empty request, StreamObserver<ApiResponseMerchantAll> observer) {
        try {
            ApiResponse<Boolean> res = merchantCommandService.deleteAllMerchantPermanent();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private MerchantResponse toProto(com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse m) {
        if (m == null) {
            return MerchantResponse.getDefaultInstance();
        }
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

    private ApiResponseMerchantDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.pointofsale.merchant.domain.responses.MerchantResponseDeleteAt> res) {
        MerchantResponseDeleteAt data = MerchantResponseDeleteAt.newBuilder()
                .setMerchantId(res.getData().getId() == null ? 0 : res.getData().getId().intValue())
                .setUserId(res.getData().getUserId() == null ? 0 : res.getData().getUserId())
                .setName(ProtoMappers.nvl(res.getData().getName()))
                .setDescription(ProtoMappers.nvl(res.getData().getDescription()))
                .setAddress(ProtoMappers.nvl(res.getData().getAddress()))
                .setContactEmail(ProtoMappers.nvl(res.getData().getContactEmail()))
                .setContactPhone(ProtoMappers.nvl(res.getData().getContactPhone()))
                .setStatus(ProtoMappers.nvl(res.getData().getStatus()))
                .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                .setDeletedAt(ProtoMappers.nvl(res.getData().getDeletedAt()))
                .build();
        return ApiResponseMerchantDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(data)
                .build();
    }

    private ApiResponseMerchantAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseMerchantAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setSuccess(Boolean.TRUE.equals(res.getData()))
                .build();
    }
}
