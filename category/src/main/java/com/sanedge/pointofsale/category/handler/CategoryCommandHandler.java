package com.sanedge.pointofsale.category.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.category.domain.requests.CreateCategoryRequest;
import com.sanedge.pointofsale.category.domain.requests.UpdateCategoryRequest;
import com.sanedge.pointofsale.category.service.CategoryCommandService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.category.ApiResponseCategory;
import com.sanedge.pointofsale.pb.category.ApiResponseCategoryAll;
import com.sanedge.pointofsale.pb.category.ApiResponseCategoryDeleteAt;
import com.sanedge.pointofsale.pb.category.CategoryCommandServiceGrpc;
import com.sanedge.pointofsale.pb.category.CategoryResponse;
import com.sanedge.pointofsale.pb.category.CategoryResponseDeleteAt;
import com.sanedge.pointofsale.pb.category.FindByIdCategoryRequest;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryCommandHandler extends CategoryCommandServiceGrpc.CategoryCommandServiceImplBase {

    private final CategoryCommandService categoryCommandService;

    @Override
    public void createCategory(com.sanedge.pointofsale.pb.category.CreateCategoryRequest request,
            StreamObserver<ApiResponseCategory> observer) {
        try {
            CreateCategoryRequest req = new CreateCategoryRequest();
            req.setName(request.getName());
            req.setDescription(request.getDescription());
            req.setSlugCategory(request.getSlugCategory());

            ApiResponse<com.sanedge.pointofsale.category.domain.responses.CategoryResponse> res = categoryCommandService
                    .createCategory(req);

            observer.onNext(ApiResponseCategory.newBuilder()
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
    public void updateCategory(com.sanedge.pointofsale.pb.category.UpdateCategoryRequest request,
            StreamObserver<ApiResponseCategory> observer) {
        try {
            UpdateCategoryRequest req = new UpdateCategoryRequest();
            req.setCategoryId(request.getCategoryId());
            req.setName(request.getName());
            req.setDescription(request.getDescription());
            req.setSlugCategory(request.getSlugCategory());

            ApiResponse<com.sanedge.pointofsale.category.domain.responses.CategoryResponse> res = categoryCommandService
                    .updateCategory(req);

            observer.onNext(ApiResponseCategory.newBuilder()
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
    public void trashedCategory(FindByIdCategoryRequest request, StreamObserver<ApiResponseCategoryDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.category.domain.responses.CategoryResponseDeleteAt> res = categoryCommandService
                    .trashedCategory(request.getCategoryId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreCategory(FindByIdCategoryRequest request, StreamObserver<ApiResponseCategoryDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.category.domain.responses.CategoryResponseDeleteAt> res = categoryCommandService
                    .restoreCategory(request.getCategoryId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteCategoryPermanent(FindByIdCategoryRequest request,
            StreamObserver<ApiResponseCategoryAll> observer) {
        try {
            ApiResponse<Boolean> res = categoryCommandService.deleteCategoryPermanent(request.getCategoryId());
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllCategory(Empty request, StreamObserver<ApiResponseCategoryAll> observer) {
        try {
            ApiResponse<Boolean> res = categoryCommandService.restoreAllCategories();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllCategoryPermanent(Empty request, StreamObserver<ApiResponseCategoryAll> observer) {
        try {
            ApiResponse<Boolean> res = categoryCommandService.deleteAllCategoriesPermanent();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private CategoryResponse toProto(com.sanedge.pointofsale.category.domain.responses.CategoryResponse r) {
        return CategoryResponse.newBuilder()
                .setCategoryId(r.getId() == null ? 0 : r.getId().intValue())
                .setName(ProtoMappers.nvl(r.getName()))
                .setDescription(ProtoMappers.nvl(r.getDescription()))
                .setSlugCategory(ProtoMappers.nvl(r.getSlugCategory()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private ApiResponseCategoryDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.pointofsale.category.domain.responses.CategoryResponseDeleteAt> res) {
        CategoryResponseDeleteAt data = CategoryResponseDeleteAt.newBuilder()
                .setCategoryId(res.getData().getId() == null ? 0 : res.getData().getId().intValue())
                .setName(ProtoMappers.nvl(res.getData().getName()))
                .setDescription(ProtoMappers.nvl(res.getData().getDescription()))
                .setSlugCategory(ProtoMappers.nvl(res.getData().getSlugCategory()))
                .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                .setDeletedAt(ProtoMappers.nvl(res.getData().getDeletedAt()))
                .build();
        return ApiResponseCategoryDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(data)
                .build();
    }

    private ApiResponseCategoryAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseCategoryAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setSuccess(Boolean.TRUE.equals(res.getData()))
                .build();
    }
}
