package com.sanedge.pointofsale.cashier.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.cashier.domain.requests.MonthCashierIdRequest;
import com.sanedge.pointofsale.cashier.domain.requests.MonthCashierMerchantRequest;
import com.sanedge.pointofsale.cashier.domain.requests.YearCashierIdRequest;
import com.sanedge.pointofsale.cashier.domain.requests.YearCashierMerchantRequest;
import com.sanedge.pointofsale.cashier.service.statsbyid.CashierSalesByIdService;
import com.sanedge.pointofsale.cashier.service.statsbymerchant.CashierSalesByMerchantService;
import com.sanedge.pointofsale.cashier.service.stats.CashierSalesService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierMonthSales;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierYearSales;
import com.sanedge.pointofsale.pb.cashier.CashierResponseMonthSales;
import com.sanedge.pointofsale.pb.cashier.CashierResponseYearSales;
import com.sanedge.pointofsale.pb.cashier.FindYearCashier;
import com.sanedge.pointofsale.pb.cashier.FindYearCashierByMerchant;
import com.sanedge.pointofsale.pb.cashier.FindYearCashierById;
import com.sanedge.pointofsale.pb.cashier.stats.CashierSalesServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashierSalesHandler extends CashierSalesServiceGrpc.CashierSalesServiceImplBase {

    private final CashierSalesService cashierSalesService;
    private final CashierSalesByIdService cashierSalesByIdService;
    private final CashierSalesByMerchantService cashierSalesByMerchantService;

    @Override
    public void findMonthSales(FindYearCashier request, StreamObserver<ApiResponseCashierMonthSales> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales>> res = cashierSalesService
                    .findMonthlySales(request.getYear());

            ApiResponseCashierMonthSales.Builder builder = ApiResponseCashierMonthSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toMonthSales(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearSales(FindYearCashier request, StreamObserver<ApiResponseCashierYearSales> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales>> res = cashierSalesService
                    .findYearlySales(request.getYear());

            ApiResponseCashierYearSales.Builder builder = ApiResponseCashierYearSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toYearSales(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findMonthSalesById(FindYearCashierById request,
            StreamObserver<ApiResponseCashierMonthSales> observer) {
        try {
            MonthCashierIdRequest req = new MonthCashierIdRequest();
            req.setCashierId(request.getCashierId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales>> res = cashierSalesByIdService
                    .findMonthlyCashierById(req);

            ApiResponseCashierMonthSales.Builder builder = ApiResponseCashierMonthSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toMonthSales(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearSalesById(FindYearCashierById request,
            StreamObserver<ApiResponseCashierYearSales> observer) {
        try {
            YearCashierIdRequest req = new YearCashierIdRequest();
            req.setCashierId(request.getCashierId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales>> res = cashierSalesByIdService
                    .findYearlyCashierById(req);

            ApiResponseCashierYearSales.Builder builder = ApiResponseCashierYearSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toYearSales(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findMonthSalesByMerchant(FindYearCashierByMerchant request,
            StreamObserver<ApiResponseCashierMonthSales> observer) {
        try {
            MonthCashierMerchantRequest req = new MonthCashierMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales>> res = cashierSalesByMerchantService
                    .findMonthlyCashierByMerchant(req);

            ApiResponseCashierMonthSales.Builder builder = ApiResponseCashierMonthSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toMonthSales(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearSalesByMerchant(FindYearCashierByMerchant request,
            StreamObserver<ApiResponseCashierYearSales> observer) {
        try {
            YearCashierMerchantRequest req = new YearCashierMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales>> res = cashierSalesByMerchantService
                    .findYearlyCashierByMerchant(req);

            ApiResponseCashierYearSales.Builder builder = ApiResponseCashierYearSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toYearSales(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private CashierResponseMonthSales toMonthSales(
            com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales r) {
        return CashierResponseMonthSales.newBuilder()
                .setMonth(ProtoMappers.nvl(r.getMonth()))
                .setCashierId(r.getCashierId() == null ? 0 : r.getCashierId())
                .setCashierName(ProtoMappers.nvl(r.getCashierName()))
                .setOrderCount(r.getOrderCount() == null ? 0 : r.getOrderCount())
                .setTotalSales(r.getTotalSales() == null ? 0L : r.getTotalSales())
                .build();
    }

    private CashierResponseYearSales toYearSales(
            com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales r) {
        return CashierResponseYearSales.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setCashierId(r.getCashierId() == null ? 0 : r.getCashierId())
                .setCashierName(ProtoMappers.nvl(r.getCashierName()))
                .setOrderCount(r.getOrderCount() == null ? 0 : r.getOrderCount())
                .setTotalSales(r.getTotalSales() == null ? 0L : r.getTotalSales())
                .build();
    }
}
