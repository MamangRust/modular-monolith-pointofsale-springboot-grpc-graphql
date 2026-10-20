package com.sanedge.pointofsale.category.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.category.domain.requests.FindAllCategory;
import com.sanedge.pointofsale.category.service.CategoryQueryService;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.category.ApiResponseCategory;
import com.sanedge.pointofsale.pb.category.ApiResponsePaginationCategory;
import com.sanedge.pointofsale.pb.category.ApiResponsePaginationCategoryDeleteAt;
import com.sanedge.pointofsale.pb.category.CategoryQueryServiceGrpc;
import com.sanedge.pointofsale.pb.category.CategoryResponse;
import com.sanedge.pointofsale.pb.category.CategoryResponseDeleteAt;
import com.sanedge.pointofsale.pb.category.FindAllCategoryRequest;
import com.sanedge.pointofsale.pb.category.FindByIdCategoryRequest;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryQueryHandler extends CategoryQueryServiceGrpc.CategoryQueryServiceImplBase {

    private final CategoryQueryService categoryQueryService;

    @Override
    public void findAll(FindAllCategoryRequest request, StreamObserver<ApiResponsePaginationCategory> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.category.domain.responses.CategoryResponse>> res = categoryQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationCategory.Builder builder = ApiResponsePaginationCategory.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProto(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findById(FindByIdCategoryRequest request, StreamObserver<ApiResponseCategory> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.category.domain.responses.CategoryResponse> res = categoryQueryService
                    .findById(request.getCategoryId());

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
    public void findByActive(FindAllCategoryRequest request,
            StreamObserver<ApiResponsePaginationCategoryDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.category.domain.responses.CategoryResponseDeleteAt>> res = categoryQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationCategoryDeleteAt.Builder builder = ApiResponsePaginationCategoryDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashed(FindAllCategoryRequest request,
            StreamObserver<ApiResponsePaginationCategoryDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.category.domain.responses.CategoryResponseDeleteAt>> res = categoryQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationCategoryDeleteAt.Builder builder = ApiResponsePaginationCategoryDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private FindAllCategory toFindAll(FindAllCategoryRequest request) {
        FindAllCategory req = new FindAllCategory();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
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

    private CategoryResponseDeleteAt toProtoDeleteAt(
            com.sanedge.pointofsale.category.domain.responses.CategoryResponseDeleteAt r) {
        return CategoryResponseDeleteAt.newBuilder()
                .setCategoryId(r.getId() == null ? 0 : r.getId().intValue())
                .setName(ProtoMappers.nvl(r.getName()))
                .setDescription(ProtoMappers.nvl(r.getDescription()))
                .setSlugCategory(ProtoMappers.nvl(r.getSlugCategory()))
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(ProtoMappers.nvl(r.getDeletedAt()))
                .build();
    }
}
