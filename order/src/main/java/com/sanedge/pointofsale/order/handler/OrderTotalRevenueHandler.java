package com.sanedge.pointofsale.order.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.order.service.stats.OrderTotalRevenueService;
import com.sanedge.pointofsale.order.service.statsbymerchant.OrderTotalRevenueByMerchantService;
import com.sanedge.pointofsale.pb.order.ApiResponseOrderMonthlyTotalRevenue;
import com.sanedge.pointofsale.pb.order.ApiResponseOrderYearlyTotalRevenue;
import com.sanedge.pointofsale.pb.order.FindYearMonthTotalRevenue;
import com.sanedge.pointofsale.pb.order.FindYearMonthTotalRevenueByMerchant;
import com.sanedge.pointofsale.pb.order.FindYearOrder;
import com.sanedge.pointofsale.pb.order.FindYearOrderByMerchant;
import com.sanedge.pointofsale.pb.order.OrderMonthlyTotalRevenueResponse;
import com.sanedge.pointofsale.pb.order.OrderYearlyTotalRevenueResponse;
import com.sanedge.pointofsale.pb.order.stats.OrderTotalRevenueServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderTotalRevenueHandler extends OrderTotalRevenueServiceGrpc.OrderTotalRevenueServiceImplBase {

    private final OrderTotalRevenueService orderTotalRevenueService;
    private final OrderTotalRevenueByMerchantService orderTotalRevenueByMerchantService;

    @Override
    public void findMonthlyTotalRevenue(FindYearMonthTotalRevenue request,
            StreamObserver<ApiResponseOrderMonthlyTotalRevenue> observer) {
        try {
            com.sanedge.pointofsale.order.domain.requests.MonthTotalRevenue req = new com.sanedge.pointofsale.order.domain.requests.MonthTotalRevenue();
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.order.domain.responses.OrderMonthlyTotalRevenueResponse>> res = orderTotalRevenueService
                    .findMonthlyStats(req);

            ApiResponseOrderMonthlyTotalRevenue.Builder builder = ApiResponseOrderMonthlyTotalRevenue.newBuilder()
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
    public void findYearlyTotalRevenue(FindYearOrder request,
            StreamObserver<ApiResponseOrderYearlyTotalRevenue> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.order.domain.responses.OrderYearlyTotalRevenueResponse>> res = orderTotalRevenueService
                    .findYearlyStats(request.getYear());

            ApiResponseOrderYearlyTotalRevenue.Builder builder = ApiResponseOrderYearlyTotalRevenue.newBuilder()
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
    public void findMonthlyTotalRevenueByMerchant(FindYearMonthTotalRevenueByMerchant request,
            StreamObserver<ApiResponseOrderMonthlyTotalRevenue> observer) {
        try {
            com.sanedge.pointofsale.order.domain.requests.MonthTotalRevenueMerchantRequest req = new com.sanedge.pointofsale.order.domain.requests.MonthTotalRevenueMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.order.domain.responses.OrderMonthlyTotalRevenueResponse>> res = orderTotalRevenueByMerchantService
                    .findMonthlyStatsByMerchant(req);

            ApiResponseOrderMonthlyTotalRevenue.Builder builder = ApiResponseOrderMonthlyTotalRevenue.newBuilder()
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
    public void findYearlyTotalRevenueByMerchant(FindYearOrderByMerchant request,
            StreamObserver<ApiResponseOrderYearlyTotalRevenue> observer) {
        try {
            com.sanedge.pointofsale.order.domain.requests.YearTotalRevenueMerchantRequest req = new com.sanedge.pointofsale.order.domain.requests.YearTotalRevenueMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.order.domain.responses.OrderYearlyTotalRevenueResponse>> res = orderTotalRevenueByMerchantService
                    .findYearlyStatsByMerchant(req);

            ApiResponseOrderYearlyTotalRevenue.Builder builder = ApiResponseOrderYearlyTotalRevenue.newBuilder()
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

    private OrderMonthlyTotalRevenueResponse toProto(
            com.sanedge.pointofsale.order.domain.responses.OrderMonthlyTotalRevenueResponse r) {
        if (r == null) {
            return OrderMonthlyTotalRevenueResponse.getDefaultInstance();
        }
        return OrderMonthlyTotalRevenueResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setMonth(ProtoMappers.nvl(r.getMonth()))
                .setTotalRevenue(r.getTotalRevenue() == null ? 0L : r.getTotalRevenue())
                .build();
    }

    private OrderYearlyTotalRevenueResponse toProto(
            com.sanedge.pointofsale.order.domain.responses.OrderYearlyTotalRevenueResponse r) {
        if (r == null) {
            return OrderYearlyTotalRevenueResponse.getDefaultInstance();
        }
        return OrderYearlyTotalRevenueResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setTotalRevenue(r.getTotalRevenue() == null ? 0L : r.getTotalRevenue())
                .build();
    }
}
