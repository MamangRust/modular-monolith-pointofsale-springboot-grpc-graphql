package com.sanedge.pointofsale.category.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.category.domain.requests.MonthTotalPrice;
import com.sanedge.pointofsale.category.domain.requests.MonthTotalPriceCategory;
import com.sanedge.pointofsale.category.domain.requests.MonthTotalPriceMerchant;
import com.sanedge.pointofsale.category.domain.requests.YearTotalPriceCategory;
import com.sanedge.pointofsale.category.domain.requests.YearTotalPriceMerchant;
import com.sanedge.pointofsale.category.service.statsbyid.CategoryTotalPriceByIdService;
import com.sanedge.pointofsale.category.service.statsbymerchant.CategoryTotalPriceByMerchantService;
import com.sanedge.pointofsale.category.service.stats.CategoryTotalPriceService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.category.ApiResponseCategoryMonthTotalPrice;
import com.sanedge.pointofsale.pb.category.ApiResponseCategoryYearTotalPrice;
import com.sanedge.pointofsale.pb.category.CategoryMonthTotalPriceResponse;
import com.sanedge.pointofsale.pb.category.CategoryYearTotalPriceResponse;
import com.sanedge.pointofsale.pb.category.FindYearMonthTotalPrice;
import com.sanedge.pointofsale.pb.category.FindYearMonthTotalPriceByMerchant;
import com.sanedge.pointofsale.pb.category.FindYearMonthTotalPriceById;
import com.sanedge.pointofsale.pb.category.FindYearTotalPrice;
import com.sanedge.pointofsale.pb.category.FindYearTotalPriceByMerchant;
import com.sanedge.pointofsale.pb.category.FindYearTotalPriceById;
import com.sanedge.pointofsale.pb.category.stats.CategoryTotalPriceServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryTotalPriceHandler extends CategoryTotalPriceServiceGrpc.CategoryTotalPriceServiceImplBase {

    private final CategoryTotalPriceService categoryTotalPriceService;
    private final CategoryTotalPriceByIdService categoryTotalPriceByIdService;
    private final CategoryTotalPriceByMerchantService categoryTotalPriceByMerchantService;

    @Override
    public void findMonthlyTotalPrices(FindYearMonthTotalPrice request,
            StreamObserver<ApiResponseCategoryMonthTotalPrice> observer) {
        try {
            MonthTotalPrice req = new MonthTotalPrice();
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesMonthlyTotalPriceResponse>> res = categoryTotalPriceService
                    .findMonthlyTotalPrice(req);

            ApiResponseCategoryMonthTotalPrice.Builder builder = ApiResponseCategoryMonthTotalPrice.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(r -> builder.addData(toMonthProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyTotalPrices(FindYearTotalPrice request,
            StreamObserver<ApiResponseCategoryYearTotalPrice> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesYearlyTotalPriceResponse>> res = categoryTotalPriceService
                    .findYearlyTotalPrice(request.getYear());

            ApiResponseCategoryYearTotalPrice.Builder builder = ApiResponseCategoryYearTotalPrice.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(r -> builder.addData(toYearProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findMonthlyTotalPricesById(FindYearMonthTotalPriceById request,
            StreamObserver<ApiResponseCategoryMonthTotalPrice> observer) {
        try {
            MonthTotalPriceCategory req = new MonthTotalPriceCategory();
            req.setCategoryId(request.getCategoryId());
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesMonthlyTotalPriceResponse>> res = categoryTotalPriceByIdService
                    .findMonthlyTotalPriceById(req);

            ApiResponseCategoryMonthTotalPrice.Builder builder = ApiResponseCategoryMonthTotalPrice.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(r -> builder.addData(toMonthProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyTotalPricesById(FindYearTotalPriceById request,
            StreamObserver<ApiResponseCategoryYearTotalPrice> observer) {
        try {
            YearTotalPriceCategory req = new YearTotalPriceCategory();
            req.setCategoryId(request.getCategoryId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesYearlyTotalPriceResponse>> res = categoryTotalPriceByIdService
                    .findYearlyTotalPriceById(req);

            ApiResponseCategoryYearTotalPrice.Builder builder = ApiResponseCategoryYearTotalPrice.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(r -> builder.addData(toYearProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findMonthlyTotalPricesByMerchant(FindYearMonthTotalPriceByMerchant request,
            StreamObserver<ApiResponseCategoryMonthTotalPrice> observer) {
        try {
            MonthTotalPriceMerchant req = new MonthTotalPriceMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());
            req.setMonth(request.getMonth());

            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesMonthlyTotalPriceResponse>> res = categoryTotalPriceByMerchantService
                    .findMonthlyTotalPriceByMerchant(req);

            ApiResponseCategoryMonthTotalPrice.Builder builder = ApiResponseCategoryMonthTotalPrice.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(r -> builder.addData(toMonthProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findYearlyTotalPricesByMerchant(FindYearTotalPriceByMerchant request,
            StreamObserver<ApiResponseCategoryYearTotalPrice> observer) {
        try {
            YearTotalPriceMerchant req = new YearTotalPriceMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesYearlyTotalPriceResponse>> res = categoryTotalPriceByMerchantService
                    .findYearlyTotalPriceByMerchant(req);

            ApiResponseCategoryYearTotalPrice.Builder builder = ApiResponseCategoryYearTotalPrice.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(r -> builder.addData(toYearProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private CategoryMonthTotalPriceResponse toMonthProto(
            com.sanedge.pointofsale.category.domain.responses.CategoriesMonthlyTotalPriceResponse r) {
        return CategoryMonthTotalPriceResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setMonth(ProtoMappers.nvl(r.getMonth()))
                .setTotalRevenue(r.getTotalRevenue() == null ? 0L : r.getTotalRevenue())
                .build();
    }

    private CategoryYearTotalPriceResponse toYearProto(
            com.sanedge.pointofsale.category.domain.responses.CategoriesYearlyTotalPriceResponse r) {
        return CategoryYearTotalPriceResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setTotalRevenue(r.getTotalRevenue() == null ? 0L : r.getTotalRevenue())
                .build();
    }
}
