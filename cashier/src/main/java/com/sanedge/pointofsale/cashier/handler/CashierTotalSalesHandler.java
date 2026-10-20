package com.sanedge.pointofsale.cashier.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.cashier.domain.requests.MonthTotalSales;
import com.sanedge.pointofsale.cashier.domain.requests.MonthTotalSalesCashier;
import com.sanedge.pointofsale.cashier.domain.requests.MonthTotalSalesMerchant;
import com.sanedge.pointofsale.cashier.domain.requests.YearTotalSalesCashier;
import com.sanedge.pointofsale.cashier.domain.requests.YearTotalSalesMerchant;
import com.sanedge.pointofsale.cashier.service.statsbyid.CashierTotalSalesByIdService;
import com.sanedge.pointofsale.cashier.service.statsbymerchant.CashierTotalSalesByMerchantService;
import com.sanedge.pointofsale.cashier.service.stats.CashierTotalSalesService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierMonthTotalSales;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashierYearTotalSales;
import com.sanedge.pointofsale.pb.cashier.CashierResponseMonthTotalSales;
import com.sanedge.pointofsale.pb.cashier.CashierResponseYearTotalSales;
import com.sanedge.pointofsale.pb.cashier.FindYearMonthTotalSales;
import com.sanedge.pointofsale.pb.cashier.FindYearMonthTotalSalesByMerchant;
import com.sanedge.pointofsale.pb.cashier.FindYearMonthTotalSalesById;
import com.sanedge.pointofsale.pb.cashier.FindYearTotalSales;
import com.sanedge.pointofsale.pb.cashier.FindYearTotalSalesByMerchant;
import com.sanedge.pointofsale.pb.cashier.FindYearTotalSalesById;
import com.sanedge.pointofsale.pb.cashier.stats.CashierTotalSalesServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashierTotalSalesHandler extends CashierTotalSalesServiceGrpc.CashierTotalSalesServiceImplBase {

    private final CashierTotalSalesService cashierTotalSalesService;
    private final CashierTotalSalesByIdService cashierTotalSalesByIdService;
    private final CashierTotalSalesByMerchantService cashierTotalSalesByMerchantService;

    @Override
    public void findMonthlyTotalSales(FindYearMonthTotalSales request,
            StreamObserver<ApiResponseCashierMonthTotalSales> observer) {
        try {
            MonthTotalSales req = new MonthTotalSales();
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthTotalSales>> res = cashierTotalSalesService
                    .findMonthlyTotalSales(req);

            ApiResponseCashierMonthTotalSales.Builder builder = ApiResponseCashierMonthTotalSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toMonthTotal(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyTotalSales(FindYearTotalSales request,
            StreamObserver<ApiResponseCashierYearTotalSales> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearTotalSales>> res = cashierTotalSalesService
                    .findYearlyTotalSales(request.getYear());

            ApiResponseCashierYearTotalSales.Builder builder = ApiResponseCashierYearTotalSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toYearTotal(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findMonthlyTotalSalesById(FindYearMonthTotalSalesById request,
            StreamObserver<ApiResponseCashierMonthTotalSales> observer) {
        try {
            MonthTotalSalesCashier req = new MonthTotalSalesCashier();
            req.setCashierId(request.getCashierId());
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthTotalSales>> res = cashierTotalSalesByIdService
                    .findMonthlyTotalSalesById(req);

            ApiResponseCashierMonthTotalSales.Builder builder = ApiResponseCashierMonthTotalSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toMonthTotal(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyTotalSalesById(FindYearTotalSalesById request,
            StreamObserver<ApiResponseCashierYearTotalSales> observer) {
        try {
            YearTotalSalesCashier req = new YearTotalSalesCashier();
            req.setCashierId(request.getCashierId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearTotalSales>> res = cashierTotalSalesByIdService
                    .findYearlyTotalSalesById(req);

            ApiResponseCashierYearTotalSales.Builder builder = ApiResponseCashierYearTotalSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toYearTotal(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findMonthlyTotalSalesByMerchant(FindYearMonthTotalSalesByMerchant request,
            StreamObserver<ApiResponseCashierMonthTotalSales> observer) {
        try {
            MonthTotalSalesMerchant req = new MonthTotalSalesMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthTotalSales>> res = cashierTotalSalesByMerchantService
                    .findMonthlyTotalSalesByMerchant(req);

            ApiResponseCashierMonthTotalSales.Builder builder = ApiResponseCashierMonthTotalSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toMonthTotal(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyTotalSalesByMerchant(FindYearTotalSalesByMerchant request,
            StreamObserver<ApiResponseCashierYearTotalSales> observer) {
        try {
            YearTotalSalesMerchant req = new YearTotalSalesMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearTotalSales>> res = cashierTotalSalesByMerchantService
                    .findYearlyTotalSalesByMerchant(req);

            ApiResponseCashierYearTotalSales.Builder builder = ApiResponseCashierYearTotalSales.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toYearTotal(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private CashierResponseMonthTotalSales toMonthTotal(
            com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthTotalSales r) {
        return CashierResponseMonthTotalSales.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setMonth(ProtoMappers.nvl(r.getMonth()))
                .setTotalSales(r.getTotalSales() == null ? 0L : r.getTotalSales())
                .build();
    }

    private CashierResponseYearTotalSales toYearTotal(
            com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearTotalSales r) {
        return CashierResponseYearTotalSales.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setTotalSales(r.getTotalSales() == null ? 0L : r.getTotalSales())
                .build();
    }
}
