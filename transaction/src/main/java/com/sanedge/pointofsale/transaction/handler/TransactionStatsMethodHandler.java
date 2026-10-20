package com.sanedge.pointofsale.transaction.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.transaction.ApiResponseTransactionMonthlyMethod;
import com.sanedge.pointofsale.pb.transaction.ApiResponseTransactionYearlyMethod;
import com.sanedge.pointofsale.pb.transaction.FindYearMonthTransaction;
import com.sanedge.pointofsale.pb.transaction.FindYearMonthTransactionByMerchant;
import com.sanedge.pointofsale.pb.transaction.FindYearTransaction;
import com.sanedge.pointofsale.pb.transaction.FindYearTransactionByMerchant;
import com.sanedge.pointofsale.pb.transaction.TransactionMonthlyMethodResponse;
import com.sanedge.pointofsale.pb.transaction.TransactionYearlyMethodResponse;
import com.sanedge.pointofsale.pb.transaction.stats.TransactionStatsMethodServiceGrpc;
import com.sanedge.pointofsale.transaction.service.stats.TransactionMethodService;
import com.sanedge.pointofsale.transaction.service.statsbymerchant.TransactionMethodByMerchantService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionStatsMethodHandler
        extends TransactionStatsMethodServiceGrpc.TransactionStatsMethodServiceImplBase {

    private final TransactionMethodService transactionMethodService;
    private final TransactionMethodByMerchantService transactionMethodByMerchantService;

    @Override
    public void findMonthlyMethodSuccess(FindYearMonthTransaction request,
            StreamObserver<ApiResponseTransactionMonthlyMethod> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionRequest req = new com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionRequest();
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyMethodResponse>> res = transactionMethodService
                    .findMonthlyMethodSuccess(req);
            observer.onNext(toMonthly(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyMethodSuccess(FindYearTransaction request,
            StreamObserver<ApiResponseTransactionYearlyMethod> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyMethodResponse>> res = transactionMethodService
                    .findYearlyMethodSuccess(request.getYear());
            observer.onNext(toYearly(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findMonthlyMethodFailed(FindYearMonthTransaction request,
            StreamObserver<ApiResponseTransactionMonthlyMethod> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionRequest req = new com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionRequest();
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyMethodResponse>> res = transactionMethodService
                    .findMonthlyMethodFailed(req);
            observer.onNext(toMonthly(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyMethodFailed(FindYearTransaction request,
            StreamObserver<ApiResponseTransactionYearlyMethod> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyMethodResponse>> res = transactionMethodService
                    .findYearlyMethodFailed(request.getYear());
            observer.onNext(toYearly(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findMonthlyMethodSuccessByMerchant(FindYearMonthTransactionByMerchant request,
            StreamObserver<ApiResponseTransactionMonthlyMethod> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionMerchantRequest req = new com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyMethodResponse>> res = transactionMethodByMerchantService
                    .findMonthlyMethodByMerchantSuccess(req);
            observer.onNext(toMonthly(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyMethodSuccessByMerchant(FindYearTransactionByMerchant request,
            StreamObserver<ApiResponseTransactionYearlyMethod> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.YearMethodTransactionMerchantRequest req = new com.sanedge.pointofsale.transaction.domain.requests.YearMethodTransactionMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyMethodResponse>> res = transactionMethodByMerchantService
                    .findYearlyMethodByMerchantSuccess(req);
            observer.onNext(toYearly(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findMonthlyMethodFailedByMerchant(FindYearMonthTransactionByMerchant request,
            StreamObserver<ApiResponseTransactionMonthlyMethod> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionMerchantRequest req = new com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyMethodResponse>> res = transactionMethodByMerchantService
                    .findMonthlyMethodByMerchantFailed(req);
            observer.onNext(toMonthly(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyMethodFailedByMerchant(FindYearTransactionByMerchant request,
            StreamObserver<ApiResponseTransactionYearlyMethod> observer) {
        try {
            com.sanedge.pointofsale.transaction.domain.requests.YearMethodTransactionMerchantRequest req = new com.sanedge.pointofsale.transaction.domain.requests.YearMethodTransactionMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyMethodResponse>> res = transactionMethodByMerchantService
                    .findYearlyMethodByMerchantFailed(req);
            observer.onNext(toYearly(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private ApiResponseTransactionMonthlyMethod toMonthly(
            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyMethodResponse>> res) {
        ApiResponseTransactionMonthlyMethod.Builder builder = ApiResponseTransactionMonthlyMethod.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            res.getData().forEach(r -> builder.addData(toProto(r)));
        }
        return builder.build();
    }

    private ApiResponseTransactionYearlyMethod toYearly(
            ApiResponse<List<com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyMethodResponse>> res) {
        ApiResponseTransactionYearlyMethod.Builder builder = ApiResponseTransactionYearlyMethod.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            res.getData().forEach(r -> builder.addData(toProto(r)));
        }
        return builder.build();
    }

    private TransactionMonthlyMethodResponse toProto(
            com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyMethodResponse r) {
        if (r == null) {
            return TransactionMonthlyMethodResponse.getDefaultInstance();
        }
        return TransactionMonthlyMethodResponse.newBuilder()
                .setMonth(ProtoMappers.nvl(r.getMonth()))
                .setPaymentMethod(ProtoMappers.nvl(r.getPaymentMethod()))
                .setTotalTransactions(r.getTotalTransactions() == null ? 0 : r.getTotalTransactions())
                .setTotalAmount(r.getTotalAmount() == null ? 0L : r.getTotalAmount())
                .build();
    }

    private TransactionYearlyMethodResponse toProto(
            com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyMethodResponse r) {
        if (r == null) {
            return TransactionYearlyMethodResponse.getDefaultInstance();
        }
        return TransactionYearlyMethodResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setPaymentMethod(ProtoMappers.nvl(r.getPaymentMethod()))
                .setTotalTransactions(r.getTotalTransactions() == null ? 0 : r.getTotalTransactions())
                .setTotalAmount(r.getTotalAmount() == null ? 0L : r.getTotalAmount())
                .build();
    }
}
