package com.sanedge.pointofsale.order.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.order.service.stats.OrderSoldoutService;
import com.sanedge.pointofsale.order.service.statsbymerchant.OrderSoldOutByMerchantService;
import com.sanedge.pointofsale.pb.order.ApiResponseOrderMonthly;
import com.sanedge.pointofsale.pb.order.ApiResponseOrderYearly;
import com.sanedge.pointofsale.pb.order.FindYearMonthOrder;
import com.sanedge.pointofsale.pb.order.FindYearMonthOrderByMerchant;
import com.sanedge.pointofsale.pb.order.FindYearOrder;
import com.sanedge.pointofsale.pb.order.FindYearOrderByMerchant;
import com.sanedge.pointofsale.pb.order.OrderMonthlyResponse;
import com.sanedge.pointofsale.pb.order.OrderYearlyResponse;
import com.sanedge.pointofsale.pb.order.stats.OrderSoldoutServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderSoldoutHandler extends OrderSoldoutServiceGrpc.OrderSoldoutServiceImplBase {

    private final OrderSoldoutService orderSoldoutService;
    private final OrderSoldOutByMerchantService orderSoldOutByMerchantService;

    @Override
    public void findMonthlyOrders(FindYearMonthOrder request, StreamObserver<ApiResponseOrderMonthly> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.order.domain.responses.OrderMonthlyResponse>> res = orderSoldoutService
                    .findMonthlyOrders(request.getYearMonth());

            ApiResponseOrderMonthly.Builder builder = ApiResponseOrderMonthly.newBuilder()
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
    public void findYearlyOrders(FindYearOrder request, StreamObserver<ApiResponseOrderYearly> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.order.domain.responses.OrderYearlyResponse>> res = orderSoldoutService
                    .findYearlyOrders(request.getYear());

            ApiResponseOrderYearly.Builder builder = ApiResponseOrderYearly.newBuilder()
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
    public void findMonthlyOrdersByMerchant(FindYearMonthOrderByMerchant request,
            StreamObserver<ApiResponseOrderMonthly> observer) {
        try {
            com.sanedge.pointofsale.order.domain.requests.MonthOrderMerchantRequest req = new com.sanedge.pointofsale.order.domain.requests.MonthOrderMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYearMonth());

            ApiResponse<List<com.sanedge.pointofsale.order.domain.responses.OrderMonthlyResponse>> res = orderSoldOutByMerchantService
                    .findMonthlyOrdersByMerchant(req);

            ApiResponseOrderMonthly.Builder builder = ApiResponseOrderMonthly.newBuilder()
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
    public void findYearlyOrdersByMerchant(FindYearOrderByMerchant request,
            StreamObserver<ApiResponseOrderYearly> observer) {
        try {
            com.sanedge.pointofsale.order.domain.requests.YearOrderMerchantRequest req = new com.sanedge.pointofsale.order.domain.requests.YearOrderMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.order.domain.responses.OrderYearlyResponse>> res = orderSoldOutByMerchantService
                    .findYearlyOrdersByMerchant(req);

            ApiResponseOrderYearly.Builder builder = ApiResponseOrderYearly.newBuilder()
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

    private OrderMonthlyResponse toProto(com.sanedge.pointofsale.order.domain.responses.OrderMonthlyResponse r) {
        if (r == null) {
            return OrderMonthlyResponse.getDefaultInstance();
        }
        return OrderMonthlyResponse.newBuilder()
                .setMonth(ProtoMappers.nvl(r.getMonth()))
                .setOrderCount(r.getOrderCount() == null ? 0 : r.getOrderCount())
                .setTotalRevenue(r.getTotalRevenue() == null ? 0L : r.getTotalRevenue())
                .setTotalItemsSold(r.getTotalItemsSold() == null ? 0 : r.getTotalItemsSold())
                .build();
    }

    private OrderYearlyResponse toProto(com.sanedge.pointofsale.order.domain.responses.OrderYearlyResponse r) {
        if (r == null) {
            return OrderYearlyResponse.getDefaultInstance();
        }
        return OrderYearlyResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setOrderCount(r.getOrderCount() == null ? 0 : r.getOrderCount())
                .setTotalRevenue(r.getTotalRevenue() == null ? 0L : r.getTotalRevenue())
                .setTotalItemsSold(r.getTotalItemsSold() == null ? 0 : r.getTotalItemsSold())
                .setActiveCashiers(r.getActiveCashiers() == null ? 0 : r.getActiveCashiers())
                .setUniqueProductsSold(r.getUniqueProductsSold() == null ? 0 : r.getUniqueProductsSold())
                .build();
    }
}
