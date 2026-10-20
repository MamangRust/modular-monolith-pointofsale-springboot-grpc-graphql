package com.sanedge.pointofsale.merchant.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponseDeleteAt;
import com.sanedge.pointofsale.merchant.service.MerchantDocumentCommandService;
import com.sanedge.pointofsale.pb.merchant_document.ApiResponseMerchantDocument;
import com.sanedge.pointofsale.pb.merchant_document.ApiResponseMerchantDocumentAll;
import com.sanedge.pointofsale.pb.merchant_document.ApiResponseMerchantDocumentDelete;
import com.sanedge.pointofsale.pb.merchant_document.ApiResponseMerchantDocumentDeleteAt;
import com.sanedge.pointofsale.pb.merchant_document.CreateMerchantDocumentRequest;
import com.sanedge.pointofsale.pb.merchant_document.FindMerchantDocumentByIdRequest;
import com.sanedge.pointofsale.pb.merchant_document.MerchantDocument;
import com.sanedge.pointofsale.pb.merchant_document.MerchantDocumentCommandServiceGrpc;
import com.sanedge.pointofsale.pb.merchant_document.UpdateMerchantDocumentRequest;
import com.sanedge.pointofsale.pb.merchant_document.UpdateMerchantDocumentStatusRequest;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

/** gRPC command surface for merchant documents (blocking style). */
@Service
@RequiredArgsConstructor
public class MerchantDocumentCommandHandler extends MerchantDocumentCommandServiceGrpc.MerchantDocumentCommandServiceImplBase {

    private final MerchantDocumentCommandService merchantDocumentCommandService;

    @Override
    public void create(CreateMerchantDocumentRequest request, StreamObserver<ApiResponseMerchantDocument> observer) {
        try {
            com.sanedge.pointofsale.merchant.domain.requests.CreateMerchantDocumentRequest req = new com.sanedge.pointofsale.merchant.domain.requests.CreateMerchantDocumentRequest();
            req.setMerchantId((long) request.getMerchantId());
            req.setDocumentType(request.getDocumentType());
            req.setDocumentUrl(request.getDocumentUrl());

            observer.onNext(toResponse(merchantDocumentCommandService.create(req)));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void update(UpdateMerchantDocumentRequest request, StreamObserver<ApiResponseMerchantDocument> observer) {
        try {
            com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantDocumentRequest req = new com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantDocumentRequest();
            req.setDocumentId((long) request.getDocumentId());
            req.setMerchantId((long) request.getMerchantId());
            req.setDocumentType(request.getDocumentType());
            req.setDocumentUrl(request.getDocumentUrl());
            req.setNote(request.getNote());
            req.setStatus(request.getStatus());

            observer.onNext(toResponse(merchantDocumentCommandService.update(req)));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void updateStatus(UpdateMerchantDocumentStatusRequest request,
            StreamObserver<ApiResponseMerchantDocument> observer) {
        try {
            com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantDocumentStatus req = new com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantDocumentStatus();
            req.setDocumentId((long) request.getDocumentId());
            req.setMerchantId((long) request.getMerchantId());
            req.setNote(request.getNote());
            req.setStatus(request.getStatus());

            observer.onNext(toResponse(merchantDocumentCommandService.updateStatus(req)));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void trashed(FindMerchantDocumentByIdRequest request,
            StreamObserver<ApiResponseMerchantDocumentDeleteAt> observer) {
        try {
            observer.onNext(toDeleteAt(merchantDocumentCommandService.trash((long) request.getDocumentId())));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restore(FindMerchantDocumentByIdRequest request,
            StreamObserver<ApiResponseMerchantDocumentDeleteAt> observer) {
        try {
            observer.onNext(toDeleteAt(merchantDocumentCommandService.restore((long) request.getDocumentId())));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deletePermanent(FindMerchantDocumentByIdRequest request,
            StreamObserver<ApiResponseMerchantDocumentDelete> observer) {
        try {
            ApiResponse<Void> res = merchantDocumentCommandService.deletePermanent((long) request.getDocumentId());
            observer.onNext(ApiResponseMerchantDocumentDelete.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAll(Empty request, StreamObserver<ApiResponseMerchantDocumentAll> observer) {
        try {
            observer.onNext(toAll(merchantDocumentCommandService.restoreAll()));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllPermanent(Empty request, StreamObserver<ApiResponseMerchantDocumentAll> observer) {
        try {
            observer.onNext(toAll(merchantDocumentCommandService.deleteAllPermanent()));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private ApiResponseMerchantDocument toResponse(ApiResponse<MerchantDocumentResponse> res) {
        return ApiResponseMerchantDocument.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(toProto(res.getData()))
                .build();
    }

    private ApiResponseMerchantDocumentDeleteAt toDeleteAt(
            ApiResponse<MerchantDocumentResponseDeleteAt> res) {
        return ApiResponseMerchantDocumentDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(toProtoDeleteAt(res.getData()))
                .build();
    }

    private ApiResponseMerchantDocumentAll toAll(ApiResponse<Void> res) {
        return ApiResponseMerchantDocumentAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .build();
    }

    static MerchantDocument toProto(MerchantDocumentResponse r) {
        if (r == null) {
            return MerchantDocument.getDefaultInstance();
        }
        MerchantDocument.Builder builder = MerchantDocument.newBuilder();
        if (r.getDocumentId() != null) {
            builder.setDocumentId(r.getDocumentId().intValue());
        }
        if (r.getMerchantId() != null) {
            builder.setMerchantId(r.getMerchantId().intValue());
        }
        builder.setDocumentType(ProtoMappers.nvl(r.getDocumentType()));
        builder.setDocumentUrl(ProtoMappers.nvl(r.getDocumentUrl()));
        builder.setStatus(ProtoMappers.nvl(r.getStatus()));
        builder.setNote(ProtoMappers.nvl(r.getNote()));
        builder.setUploadedAt(ProtoMappers.nvl(r.getCreatedAt()));
        builder.setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()));
        return builder.build();
    }

    static com.sanedge.pointofsale.pb.merchant_document.MerchantDocumentDeleteAt toProtoDeleteAt(
            MerchantDocumentResponseDeleteAt r) {
        if (r == null) {
            return com.sanedge.pointofsale.pb.merchant_document.MerchantDocumentDeleteAt.getDefaultInstance();
        }
        com.sanedge.pointofsale.pb.merchant_document.MerchantDocumentDeleteAt.Builder builder = com.sanedge.pointofsale.pb.merchant_document.MerchantDocumentDeleteAt
                .newBuilder();
        if (r.getDocumentId() != null) {
            builder.setDocumentId(r.getDocumentId().intValue());
        }
        if (r.getMerchantId() != null) {
            builder.setMerchantId(r.getMerchantId().intValue());
        }
        builder.setDocumentType(ProtoMappers.nvl(r.getDocumentType()));
        builder.setDocumentUrl(ProtoMappers.nvl(r.getDocumentUrl()));
        builder.setStatus(ProtoMappers.nvl(r.getStatus()));
        builder.setNote(ProtoMappers.nvl(r.getNote()));
        builder.setUploadedAt(ProtoMappers.nvl(r.getCreatedAt()));
        builder.setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()));
        if (r.getDeletedAt() != null) {
            builder.setDeletedAt(com.google.protobuf.StringValue.of(r.getDeletedAt()));
        }
        return builder.build();
    }
}
