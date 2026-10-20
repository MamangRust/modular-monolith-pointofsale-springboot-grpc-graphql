package com.sanedge.pointofsale.transaction.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.transaction.ApiResponseTransaction;
import com.sanedge.pointofsale.pb.transaction.ApiResponseTransactionAll;
import com.sanedge.pointofsale.pb.transaction.ApiResponseTransactionDeleteAt;
import com.sanedge.pointofsale.pb.transaction.FindByIdTransactionRequest;
import com.sanedge.pointofsale.pb.transaction.TransactionCommandServiceGrpc;
import com.sanedge.pointofsale.pb.transaction.TransactionResponse;
import com.sanedge.pointofsale.pb.transaction.TransactionResponseDeleteAt;
import com.sanedge.pointofsale.transaction.service.TransactionCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionCommandHandler extends TransactionCommandServiceGrpc.TransactionCommandServiceImplBase {

    private final TransactionCommandService transactionCommandService;

    @Override
    public void createTransaction(com.sanedge.pointofsale.pb.transaction.CreateTransactionRequest request,
            StreamObserver<ApiResponseTransaction> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.CreateTransactionRequest req = new com.sanedge.pointofsale.transaction.domain.requests.CreateTransactionRequest();
            req.setOrderID(request.getOrderId());
            req.setMerchantID(request.getMerchantId());
            req.setPaymentMethod(request.getPaymentMethod());
            req.setAmount(request.getAmount());

            ApiResponse<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse> res = transactionCommandService
                    .create(req);

            observer.onNext(ApiResponseTransaction.newBuilder()
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
    public void updateTransaction(com.sanedge.pointofsale.pb.transaction.UpdateTransactionRequest request,
            StreamObserver<ApiResponseTransaction> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.UpdateTransactionRequest req = new com.sanedge.pointofsale.transaction.domain.requests.UpdateTransactionRequest();
            req.setTransactionID(request.getTransactionId());
            req.setOrderID(request.getOrderId());
            req.setMerchantID(request.getMerchantId());
            req.setPaymentMethod(request.getPaymentMethod());
            req.setAmount(request.getAmount());
            req.setPaymentStatus(request.getPaymentStatus());

            ApiResponse<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse> res = transactionCommandService
                    .update(req);

            observer.onNext(ApiResponseTransaction.newBuilder()
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
    public void trashedTransaction(FindByIdTransactionRequest request,
            StreamObserver<ApiResponseTransactionDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponseDeleteAt> res = transactionCommandService
                    .trash(request.getTransactionId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreTransaction(FindByIdTransactionRequest request,
            StreamObserver<ApiResponseTransactionDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponseDeleteAt> res = transactionCommandService
                    .restore(request.getTransactionId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteTransactionPermanent(FindByIdTransactionRequest request,
            StreamObserver<ApiResponseTransactionAll> observer) {
        try {
            ApiResponse<Boolean> res = transactionCommandService.delete(request.getTransactionId());
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllTransaction(Empty request, StreamObserver<ApiResponseTransactionAll> observer) {
        try {
            ApiResponse<Boolean> res = transactionCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllTransactionPermanent(Empty request, StreamObserver<ApiResponseTransactionAll> observer) {
        try {
            ApiResponse<Boolean> res = transactionCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private TransactionResponse toProto(
            com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse r) {
        if (r == null) {
            return TransactionResponse.getDefaultInstance();
        }
        return TransactionResponse.newBuilder()
                .setTransactionId(r.getId() == null ? 0 : r.getId().intValue())
                .setOrderId(r.getOrderId() == null ? 0 : r.getOrderId())
                .setMerchantId(r.getMerchantId() == null ? 0 : r.getMerchantId())
                .setPaymentMethod(ProtoMappers.nvl(r.getPaymentMethod()))
                .setAmount(r.getAmount() == null ? 0 : r.getAmount())
                .setPaymentStatus(ProtoMappers.nvl(r.getPaymentStatus()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private TransactionResponseDeleteAt toProtoDeleteAt(
            com.sanedge.pointofsale.transaction.domain.responses.TransactionResponseDeleteAt r) {
        if (r == null) {
            return TransactionResponseDeleteAt.getDefaultInstance();
        }
        return TransactionResponseDeleteAt.newBuilder()
                .setTransactionId(r.getId() == null ? 0 : r.getId().intValue())
                .setOrderId(r.getOrderId() == null ? 0 : r.getOrderId())
                .setMerchantId(r.getMerchantId() == null ? 0 : r.getMerchantId())
                .setPaymentMethod(ProtoMappers.nvl(r.getPaymentMethod()))
                .setAmount(r.getAmount() == null ? 0 : r.getAmount())
                .setPaymentStatus(ProtoMappers.nvl(r.getPaymentStatus()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(ProtoMappers.nvl(r.getDeletedAt()))
                .build();
    }

    private ApiResponseTransactionDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponseDeleteAt> res) {
        return ApiResponseTransactionDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(toProtoDeleteAt(res.getData()))
                .build();
    }

    private ApiResponseTransactionAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseTransactionAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setSuccess(Boolean.TRUE.equals(res.getData()))
                .build();
    }
}
