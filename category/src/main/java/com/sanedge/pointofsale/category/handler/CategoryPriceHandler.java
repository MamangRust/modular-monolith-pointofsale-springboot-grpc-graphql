package com.sanedge.pointofsale.category.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.category.domain.requests.MonthPriceId;
import com.sanedge.pointofsale.category.domain.requests.MonthPriceMerchant;
import com.sanedge.pointofsale.category.domain.requests.YearPriceId;
import com.sanedge.pointofsale.category.domain.requests.YearPriceMerchant;
import com.sanedge.pointofsale.category.service.statsbyid.CategoryPriceByIdService;
import com.sanedge.pointofsale.category.service.statsbymerchant.CategoryPriceByMerchantService;
import com.sanedge.pointofsale.category.service.stats.CategoryPriceService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.category.ApiResponseCategoryMonthPrice;
import com.sanedge.pointofsale.pb.category.ApiResponseCategoryYearPrice;
import com.sanedge.pointofsale.pb.category.CategoryMonthPriceResponse;
import com.sanedge.pointofsale.pb.category.CategoryYearPriceResponse;
import com.sanedge.pointofsale.pb.category.FindYearCategory;
import com.sanedge.pointofsale.pb.category.FindYearCategoryByMerchant;
import com.sanedge.pointofsale.pb.category.FindYearCategoryById;
import com.sanedge.pointofsale.pb.category.stats.CategoryPriceServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryPriceHandler extends CategoryPriceServiceGrpc.CategoryPriceServiceImplBase {

    private final CategoryPriceService categoryPriceService;
    private final CategoryPriceByIdService categoryPriceByIdService;
    private final CategoryPriceByMerchantService categoryPriceByMerchantService;

    @Override
    public void findMonthPrice(FindYearCategory request, StreamObserver<ApiResponseCategoryMonthPrice> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesMonthPriceResponse>> res = categoryPriceService
                    .findMonthPrice(request.getYear());

            ApiResponseCategoryMonthPrice.Builder builder = ApiResponseCategoryMonthPrice.newBuilder()
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
    public void findYearPrice(FindYearCategory request, StreamObserver<ApiResponseCategoryYearPrice> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesYearPriceResponse>> res = categoryPriceService
                    .findYearPrice(request.getYear());

            ApiResponseCategoryYearPrice.Builder builder = ApiResponseCategoryYearPrice.newBuilder()
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
    public void findMonthPriceById(FindYearCategoryById request, StreamObserver<ApiResponseCategoryMonthPrice> observer) {
        try {
            MonthPriceId req = new MonthPriceId();
            req.setCategoryId(request.getCategoryId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesMonthPriceResponse>> res = categoryPriceByIdService
                    .findMonthPriceById(req);

            ApiResponseCategoryMonthPrice.Builder builder = ApiResponseCategoryMonthPrice.newBuilder()
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
    public void findYearPriceById(FindYearCategoryById request, StreamObserver<ApiResponseCategoryYearPrice> observer) {
        try {
            YearPriceId req = new YearPriceId();
            req.setCategoryId(request.getCategoryId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesYearPriceResponse>> res = categoryPriceByIdService
                    .findYearPriceById(req);

            ApiResponseCategoryYearPrice.Builder builder = ApiResponseCategoryYearPrice.newBuilder()
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
    public void findMonthPriceByMerchant(FindYearCategoryByMerchant request,
            StreamObserver<ApiResponseCategoryMonthPrice> observer) {
        try {
            MonthPriceMerchant req = new MonthPriceMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesMonthPriceResponse>> res = categoryPriceByMerchantService
                    .findMonthPriceByMerchant(req);

            ApiResponseCategoryMonthPrice.Builder builder = ApiResponseCategoryMonthPrice.newBuilder()
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
    public void findYearPriceByMerchant(FindYearCategoryByMerchant request,
            StreamObserver<ApiResponseCategoryYearPrice> observer) {
        try {
            YearPriceMerchant req = new YearPriceMerchant();
            req.setMerchantId(request.getMerchantId());
            req.setYear(request.getYear());

            ApiResponse<List<com.sanedge.pointofsale.category.domain.responses.CategoriesYearPriceResponse>> res = categoryPriceByMerchantService
                    .findYearPriceByMerchant(req);

            ApiResponseCategoryYearPrice.Builder builder = ApiResponseCategoryYearPrice.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            res.getData().forEach(r -> builder.addData(toYearProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private CategoryMonthPriceResponse toMonthProto(
            com.sanedge.pointofsale.category.domain.responses.CategoriesMonthPriceResponse r) {
        return CategoryMonthPriceResponse.newBuilder()
                .setMonth(ProtoMappers.nvl(r.getMonth()))
                .setCategoryId(r.getCategoryId() == null ? 0 : r.getCategoryId())
                .setCategoryName(ProtoMappers.nvl(r.getCategoryName()))
                .setOrderCount(r.getOrderCount() == null ? 0 : r.getOrderCount())
                .setItemsSold(r.getItemsSold() == null ? 0 : r.getItemsSold())
                .setTotalRevenue(r.getTotalRevenue() == null ? 0L : r.getTotalRevenue())
                .build();
    }

    private CategoryYearPriceResponse toYearProto(
            com.sanedge.pointofsale.category.domain.responses.CategoriesYearPriceResponse r) {
        return CategoryYearPriceResponse.newBuilder()
                .setYear(ProtoMappers.nvl(r.getYear()))
                .setCategoryId(r.getCategoryId() == null ? 0 : r.getCategoryId())
                .setCategoryName(ProtoMappers.nvl(r.getCategoryName()))
                .setOrderCount(r.getOrderCount() == null ? 0 : r.getOrderCount())
                .setItemsSold(r.getItemsSold() == null ? 0 : r.getItemsSold())
                .setTotalRevenue(r.getTotalRevenue() == null ? 0L : r.getTotalRevenue())
                .setUniqueProductsSold(r.getUniqueProductsSold() == null ? 0 : r.getUniqueProductsSold())
                .build();
    }
}
