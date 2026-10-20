package com.sanedge.pointofsale.cashier.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.cashier.service.CashierCommandService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashier;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierAll;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierDeleteAt;
import com.sanedge.pointofsale.pb.cashier.CashierCommandServiceGrpc;
import com.sanedge.pointofsale.pb.cashier.CashierResponse;
import com.sanedge.pointofsale.pb.cashier.CashierResponseDeleteAt;
import com.sanedge.pointofsale.pb.cashier.CreateCashierRequest;
import com.sanedge.pointofsale.pb.cashier.FindByIdCashierRequest;
import com.sanedge.pointofsale.pb.cashier.UpdateCashierRequest;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashierCommandHandler extends CashierCommandServiceGrpc.CashierCommandServiceImplBase {

    private final CashierCommandService cashierCommandService;

    @Override
    public void createCashier(CreateCashierRequest request, StreamObserver<ApiResponseCashier> observer) {
        try {
            com.sanedge.pointofsale.cashier.domain.requests.CreateCashierRequest req = new com.sanedge.pointofsale.cashier.domain.requests.CreateCashierRequest();
            req.setMerchantId(request.getMerchantId());
            req.setUserId(request.getUserId());
            req.setName(request.getName());

            ApiResponse<com.sanedge.pointofsale.cashier.domain.responses.CashierResponse> res = cashierCommandService
                    .createCashier(req);

            observer.onNext(toCashier(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void updateCashier(UpdateCashierRequest request, StreamObserver<ApiResponseCashier> observer) {
        try {
            com.sanedge.pointofsale.cashier.domain.requests.UpdateCashierRequest req = new com.sanedge.pointofsale.cashier.domain.requests.UpdateCashierRequest();
            req.setCashierId(request.getCashierId());
            req.setName(request.getName());

            ApiResponse<com.sanedge.pointofsale.cashier.domain.responses.CashierResponse> res = cashierCommandService
                    .updateCashier(req);

            observer.onNext(toCashier(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void trashedCashier(FindByIdCashierRequest request, StreamObserver<ApiResponseCashierDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt> res = cashierCommandService
                    .trashedCashier(request.getCashierId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreCashier(FindByIdCashierRequest request, StreamObserver<ApiResponseCashierDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt> res = cashierCommandService
                    .restoreCashier(request.getCashierId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteCashierPermanent(FindByIdCashierRequest request, StreamObserver<ApiResponseCashierAll> observer) {
        try {
            ApiResponse<Boolean> res = cashierCommandService.deleteCashierPermanent(request.getCashierId());
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllCashier(Empty request, StreamObserver<ApiResponseCashierAll> observer) {
        try {
            ApiResponse<Boolean> res = cashierCommandService.restoreAllCashier();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllCashierPermanent(Empty request, StreamObserver<ApiResponseCashierAll> observer) {
        try {
            ApiResponse<Boolean> res = cashierCommandService.deleteAllCashierPermanent();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private ApiResponseCashier toCashier(
            ApiResponse<com.sanedge.pointofsale.cashier.domain.responses.CashierResponse> res) {
        ApiResponseCashier.Builder builder = ApiResponseCashier.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            builder.setData(CashierResponse.newBuilder()
                    .setCashierId(res.getData().getId() == null ? 0 : res.getData().getId())
                    .setMerchantId(res.getData().getMerchantId() == null ? 0 : res.getData().getMerchantId())
                    .setName(ProtoMappers.nvl(res.getData().getName()))
                    .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                    .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                    .build());
        }
        return builder.build();
    }

    private ApiResponseCashierDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt> res) {
        ApiResponseCashierDeleteAt.Builder builder = ApiResponseCashierDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            builder.setData(CashierResponseDeleteAt.newBuilder()
                    .setCashierId(res.getData().getId() == null ? 0 : res.getData().getId())
                    .setMerchantId(res.getData().getMerchantId() == null ? 0 : res.getData().getMerchantId())
                    .setName(ProtoMappers.nvl(res.getData().getName()))
                    .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                    .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                    .setDeletedAt(ProtoMappers.nvl(res.getData().getDeletedAt()))
                    .build());
        }
        return builder.build();
    }

    private ApiResponseCashierAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseCashierAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setSuccess(Boolean.TRUE.equals(res.getData()))
                .build();
    }
}
