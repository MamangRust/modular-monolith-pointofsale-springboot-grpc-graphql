package com.sanedge.pointofsale.transaction.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.transaction.ApiResponseTransactionMonthlyAmountFailed;
import com.sanedge.pointofsale.pb.transaction.ApiResponseTransactionMonthlyAmountSuccess;
import com.sanedge.pointofsale.pb.transaction.ApiResponseTransactionYearlyAmountFailed;
import com.sanedge.pointofsale.pb.transaction.ApiResponseTransactionYearlyAmountSuccess;
import com.sanedge.pointofsale.pb.transaction.FindYearMonthTransaction;
import com.sanedge.pointofsale.pb.transaction.FindYearMonthTransactionByMerchant;
import com.sanedge.pointofsale.pb.transaction.FindYearTransaction;
import com.sanedge.pointofsale.pb.transaction.FindYearTransactionByMerchant;
import com.sanedge.pointofsale.pb.transaction.TransactionMonthlyAmountFailedResponse;
import com.sanedge.pointofsale.pb.transaction.TransactionMonthlyAmountSuccessResponse;
import com.sanedge.pointofsale.pb.transaction.TransactionYearlyAmountFailedResponse;
import com.sanedge.pointofsale.pb.transaction.TransactionYearlyAmountSuccessResponse;
import com.sanedge.pointofsale.pb.transaction.stats.TransactionStatsAmountServiceGrpc;
import com.sanedge.pointofsale.transaction.service.stats.TransactionAmountService;
import com.sanedge.pointofsale.transaction.service.statsbymerchant.TransactionAmountByMerchantService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionStatsAmountHandler
        extends TransactionStatsAmountServiceGrpc.TransactionStatsAmountServiceImplBase {

    private final TransactionAmountService transactionAmountService;
    private final TransactionAmountByMerchantService transactionAmountByMerchantService;

    @Override
    public void findMonthlyTransactionSuccess(FindYearMonthTransaction request,
            StreamObserver<ApiResponseTransactionMonthlyAmountSuccess> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionRequest req = new com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionRequest();
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountSuccessResponse>> res = transactionAmountService
                    .findMonthlyAmountSuccess(req);

            ApiResponseTransactionMonthlyAmountSuccess.Builder builder = ApiResponseTransactionMonthlyAmountSuccess
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
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
    public void findYearlyTransactionSuccess(FindYearTransaction request,
            StreamObserver<ApiResponseTransactionYearlyAmountSuccess> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountSuccessResponse>> res = transactionAmountService
                    .findYearlyAmountSuccess(request.getYear());

            ApiResponseTransactionYearlyAmountSuccess.Builder builder = ApiResponseTransactionYearlyAmountSuccess
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
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
    public void findMonthlyTransactionFailed(FindYearMonthTransaction request,
            StreamObserver<ApiResponseTransactionMonthlyAmountFailed> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionRequest req = new com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionRequest();
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountFailedResponse>> res = transactionAmountService
                    .findMonthlyAmountFailed(req);

            ApiResponseTransactionMonthlyAmountFailed.Builder builder = ApiResponseTransactionMonthlyAmountFailed
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
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
    public void findYearlyTransactionFailed(FindYearTransaction request,
            StreamObserver<ApiResponseTransactionYearlyAmountFailed> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountFailedResponse>> res = transactionAmountService
                    .findYearlyAmountFailed(request.getYear());

            ApiResponseTransactionYearlyAmountFailed.Builder builder = ApiResponseTransactionYearlyAmountFailed
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
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
    public void findMonthlyTransactionSuccessByMerchant(FindYearMonthTransactionByMerchant request,
            StreamObserver<ApiResponseTransactionMonthlyAmountSuccess> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionMerchant req = new com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountSuccessResponse>> res = transactionAmountByMerchantService
                    .findMonthlyAmountSuccessByMerchant(req);

            ApiResponseTransactionMonthlyAmountSuccess.Builder builder = ApiResponseTransactionMonthlyAmountSuccess
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
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
    public void findYearlyTransactionSuccessByMerchant(FindYearTransactionByMerchant request,
            StreamObserver<ApiResponseTransactionYearlyAmountSuccess> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.YearAmountTransactionMerchant req = new com.sanedge.pointofsale.transaction.domain.requests.YearAmountTransactionMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountSuccessResponse>> res = transactionAmountByMerchantService
                    .findYearlyAmountSuccessByMerchant(req);

            ApiResponseTransactionYearlyAmountSuccess.Builder builder = ApiResponseTransactionYearlyAmountSuccess
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
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
    public void findMonthlyTransactionFailedByMerchant(FindYearMonthTransactionByMerchant request,
            StreamObserver<ApiResponseTransactionMonthlyAmountFailed> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionMerchant req = new com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountFailedResponse>> res = transactionAmountByMerchantService
                    .findMonthlyAmountFailedByMerchant(req);

            ApiResponseTransactionMonthlyAmountFailed.Builder builder = ApiResponseTransactionMonthlyAmountFailed
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
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
    public void findYearlyTransactionFailedByMerchant(FindYearTransactionByMerchant request,
            StreamObserver<ApiResponseTransactionYearlyAmountFailed> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.YearAmountTransactionMerchant req = new com.sanedge.pointofsale.transaction.domain.requests.YearAmountTransactionMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountFailedResponse>> res = transactionAmountByMerchantService
                    .findYearlyAmountFailedByMerchant(req);

            ApiResponseTransactionYearlyAmountFailed.Builder builder = ApiResponseTransactionYearlyAmountFailed
                    .newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProto(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private TransactionMonthlyAmountSuccessResponse toProto(
            com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountSuccessResponse r) {
        if (r == null) {
            return TransactionMonthlyAmountSuccessResponse.getDefaultInstance();
        }
        return TransactionMonthlyAmountSuccessResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setMonth(ProtoMappers.nvl(r.getMonth()))
                .setTotalSuccess(r.getTotalSuccess() == null ? 0 : r.getTotalSuccess())
                .setTotalAmount(r.getTotalAmount() == null ? 0L : r.getTotalAmount())
                .build();
    }

    private TransactionYearlyAmountSuccessResponse toProto(
            com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountSuccessResponse r) {
        if (r == null) {
            return TransactionYearlyAmountSuccessResponse.getDefaultInstance();
        }
        return TransactionYearlyAmountSuccessResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setTotalSuccess(r.getTotalSuccess() == null ? 0 : r.getTotalSuccess())
                .setTotalAmount(r.getTotalAmount() == null ? 0L : r.getTotalAmount())
                .build();
    }

    private TransactionMonthlyAmountFailedResponse toProto(
            com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountFailedResponse r) {
        if (r == null) {
            return TransactionMonthlyAmountFailedResponse.getDefaultInstance();
        }
        return TransactionMonthlyAmountFailedResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setMonth(ProtoMappers.nvl(r.getMonth()))
                .setTotalFailed(r.getTotalFailed() == null ? 0 : r.getTotalFailed())
                .setTotalAmount(r.getTotalAmount() == null ? 0L : r.getTotalAmount())
                .build();
    }

    private TransactionYearlyAmountFailedResponse toProto(
            com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountFailedResponse r) {
        if (r == null) {
            return TransactionYearlyAmountFailedResponse.getDefaultInstance();
        }
        return TransactionYearlyAmountFailedResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setTotalFailed(r.getTotalFailed() == null ? 0 : r.getTotalFailed())
                .setTotalAmount(r.getTotalAmount() == null ? 0L : r.getTotalAmount())
                .build();
    }
}
