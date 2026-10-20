package com.sanedge.pointofsale.product.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.pb.product.ApiResponseProduct;
import com.sanedge.pointofsale.pb.product.ApiResponseProductAll;
import com.sanedge.pointofsale.pb.product.ApiResponseProductDeleteAt;
import com.sanedge.pointofsale.pb.product.ProductCommandServiceGrpc;
import com.sanedge.pointofsale.pb.product.ProductResponse;
import com.sanedge.pointofsale.pb.product.ProductResponseDeleteAt;
import com.sanedge.pointofsale.product.service.ProductCommandService;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCommandHandler extends ProductCommandServiceGrpc.ProductCommandServiceImplBase {

    private final ProductCommandService productCommandService;

    @Override
    public void createProduct(com.sanedge.pointofsale.pb.product.CreateProductRequest request,
            StreamObserver<ApiResponseProduct> observer) {
        try {
            com.sanedge.pointofsale.product.domain.requests.CreateProductRequest req = new com.sanedge.pointofsale.product.domain.requests.CreateProductRequest();
            req.setMerchantId(request.getMerchantId());
            req.setCategoryId(request.getCategoryId());
            req.setName(request.getName());
            req.setDescription(request.getDescription());
            req.setPrice(request.getPrice());
            req.setCountInStock(request.getCountInStock());
            req.setBrand(request.getBrand());
            req.setWeight(parseInteger(request.getWeight()));
            req.setRating((int) request.getRating());
            req.setSlugProduct(request.getSlugProduct());
            req.setImageProduct(request.getImageProduct());

            ApiResponse<com.sanedge.pointofsale.product.domain.responses.ProductResponse> res = productCommandService
                    .createProduct(req);

            observer.onNext(toProduct(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void updateProduct(com.sanedge.pointofsale.pb.product.UpdateProductRequest request,
            StreamObserver<ApiResponseProduct> observer) {
        try {
            com.sanedge.pointofsale.product.domain.requests.UpdateProductRequest req = new com.sanedge.pointofsale.product.domain.requests.UpdateProductRequest();
            req.setProductId(request.getProductId());
            req.setMerchantId(request.getMerchantId());
            req.setCategoryId(request.getCategoryId());
            req.setName(request.getName());
            req.setDescription(request.getDescription());
            req.setPrice(request.getPrice());
            req.setCountInStock(request.getCountInStock());
            req.setBrand(request.getBrand());
            req.setWeight(parseInteger(request.getWeight()));
            req.setRating((int) request.getRating());
            req.setSlugProduct(request.getSlugProduct());
            req.setImageProduct(request.getImageProduct());

            ApiResponse<com.sanedge.pointofsale.product.domain.responses.ProductResponse> res = productCommandService
                    .updateProduct(req);

            observer.onNext(toProduct(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void trashedProduct(com.sanedge.pointofsale.pb.product.FindByIdProductRequest request,
            StreamObserver<ApiResponseProductDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.product.domain.responses.ProductResponseDeleteAt> res = productCommandService
                    .trashedProduct(request.getProductId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreProduct(com.sanedge.pointofsale.pb.product.FindByIdProductRequest request,
            StreamObserver<ApiResponseProductDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.product.domain.responses.ProductResponseDeleteAt> res = productCommandService
                    .restoreProduct(request.getProductId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteProductPermanent(com.sanedge.pointofsale.pb.product.FindByIdProductRequest request,
            StreamObserver<ApiResponseProductAll> observer) {
        try {
            ApiResponse<Boolean> res = productCommandService.deleteProductPermanent(request.getProductId());
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllProduct(Empty request, StreamObserver<ApiResponseProductAll> observer) {
        try {
            ApiResponse<Boolean> res = productCommandService.restoreAllProducts();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllProductPermanent(Empty request, StreamObserver<ApiResponseProductAll> observer) {
        try {
            ApiResponse<Boolean> res = productCommandService.deleteAllProductsPermanent();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ApiResponseProduct toProduct(
            ApiResponse<com.sanedge.pointofsale.product.domain.responses.ProductResponse> res) {
        ApiResponseProduct.Builder builder = ApiResponseProduct.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            builder.setData(ProductResponse.newBuilder()
                    .setProductId(res.getData().getId() == null ? 0 : res.getData().getId().intValue())
                    .setMerchantId(res.getData().getMerchantId() == null ? 0 : res.getData().getMerchantId())
                    .setCategoryId(res.getData().getCategoryId() == null ? 0 : res.getData().getCategoryId())
                    .setName(ProtoMappers.nvl(res.getData().getName()))
                    .setDescription(ProtoMappers.nvl(res.getData().getDescription()))
                    .setPrice(res.getData().getPrice() == null ? 0 : res.getData().getPrice())
                    .setCountInStock(res.getData().getCountInStock() == null ? 0 : res.getData().getCountInStock())
                    .setBrand(ProtoMappers.nvl(res.getData().getBrand()))
                    .setWeight(res.getData().getWeight() == null ? "" : String.valueOf(res.getData().getWeight()))
                    .setSlugProduct(ProtoMappers.nvl(res.getData().getSlugProduct()))
                    .setImageProduct(ProtoMappers.nvl(res.getData().getImageProduct()))
                    .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                    .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                    .build());
        }
        return builder.build();
    }

    private ApiResponseProductDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.pointofsale.product.domain.responses.ProductResponseDeleteAt> res) {
        ApiResponseProductDeleteAt.Builder builder = ApiResponseProductDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            builder.setData(ProductResponseDeleteAt.newBuilder()
                    .setProductId(res.getData().getId() == null ? 0 : res.getData().getId().intValue())
                    .setMerchantId(res.getData().getMerchantId() == null ? 0 : res.getData().getMerchantId())
                    .setCategoryId(res.getData().getCategoryId() == null ? 0 : res.getData().getCategoryId())
                    .setName(ProtoMappers.nvl(res.getData().getName()))
                    .setDescription(ProtoMappers.nvl(res.getData().getDescription()))
                    .setPrice(res.getData().getPrice() == null ? 0 : res.getData().getPrice())
                    .setCountInStock(res.getData().getCountInStock() == null ? 0 : res.getData().getCountInStock())
                    .setBrand(ProtoMappers.nvl(res.getData().getBrand()))
                    .setWeight(res.getData().getWeight() == null ? "" : String.valueOf(res.getData().getWeight()))
                    .setRating(res.getData().getRating() == null ? 0f : res.getData().getRating())
                    .setSlugProduct(ProtoMappers.nvl(res.getData().getSlugProduct()))
                    .setImageProduct(ProtoMappers.nvl(res.getData().getImageProduct()))
                    .setCreatedAt(ProtoMappers.nvl(res.getData().getCreatedAt()))
                    .setUpdatedAt(ProtoMappers.nvl(res.getData().getUpdatedAt()))
                    .setDeletedAt(ProtoMappers.nvl(res.getData().getDeletedAt()))
                    .build());
        }
        return builder.build();
    }

    private ApiResponseProductAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseProductAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setSuccess(Boolean.TRUE.equals(res.getData()))
                .build();
    }
}
