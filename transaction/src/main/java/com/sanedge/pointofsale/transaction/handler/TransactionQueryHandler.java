package com.sanedge.pointofsale.transaction.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.transaction.ApiResponsePaginationTransaction;
import com.sanedge.pointofsale.pb.transaction.ApiResponsePaginationTransactionDeleteAt;
import com.sanedge.pointofsale.pb.transaction.ApiResponseTransaction;
import com.sanedge.pointofsale.pb.transaction.FindAllTransactionRequest;
import com.sanedge.pointofsale.pb.transaction.FindByIdTransactionRequest;
import com.sanedge.pointofsale.pb.transaction.FindByMerchantTransactionRequest;
import com.sanedge.pointofsale.pb.transaction.FindByOrderTransactionRequest;
import com.sanedge.pointofsale.pb.transaction.TransactionQueryServiceGrpc;
import com.sanedge.pointofsale.pb.transaction.TransactionResponse;
import com.sanedge.pointofsale.pb.transaction.TransactionResponseDeleteAt;
import com.sanedge.pointofsale.transaction.service.TransactionQueryService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionQueryHandler extends TransactionQueryServiceGrpc.TransactionQueryServiceImplBase {

    private final TransactionQueryService transactionQueryService;

    @Override
    public void findAll(FindAllTransactionRequest request,
            StreamObserver<ApiResponsePaginationTransaction> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse>> res = transactionQueryService
                    .findAllTransactions(toFindAll(request));

            ApiResponsePaginationTransaction.Builder builder = ApiResponsePaginationTransaction.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProto(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findById(FindByIdTransactionRequest request, StreamObserver<ApiResponseTransaction> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse> res = transactionQueryService
                    .findById(request.getTransactionId());

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
    public void findByMerchant(FindByMerchantTransactionRequest request,
            StreamObserver<ApiResponsePaginationTransaction> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.FindAllTransactionByMerchantRequest req = new com.sanedge.pointofsale.transaction.domain.requests.FindAllTransactionByMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setSearch(request.getSearch());
            req.setPage(request.getPage());
            req.setPageSize(request.getPageSize());

            ApiResponsePagination<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse>> res = transactionQueryService
                    .findByMerchant(req);

            ApiResponsePaginationTransaction.Builder builder = ApiResponsePaginationTransaction.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProto(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByOrderId(FindByOrderTransactionRequest request, StreamObserver<ApiResponseTransaction> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse> res = transactionQueryService
                    .findByOrderId(request.getOrderId());

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
    public void findByActive(FindAllTransactionRequest request,
            StreamObserver<ApiResponsePaginationTransactionDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponseDeleteAt>> res = transactionQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationTransactionDeleteAt.Builder builder = ApiResponsePaginationTransactionDeleteAt
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashed(FindAllTransactionRequest request,
            StreamObserver<ApiResponsePaginationTransactionDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionResponseDeleteAt>> res = transactionQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationTransactionDeleteAt.Builder builder = ApiResponsePaginationTransactionDeleteAt
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private com.sanedge.pointofsale.transaction.domain.requests.FindAllTransactionRequest toFindAll(
            FindAllTransactionRequest request) {
        com.sanedge.pointofsale.transaction.domain.requests.FindAllTransactionRequest req = new com.sanedge.pointofsale.transaction.domain.requests.FindAllTransactionRequest();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
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
}
