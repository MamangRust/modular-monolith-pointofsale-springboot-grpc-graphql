package com.sanedge.pointofsale.gateway.resolver;

import java.io.File;
import java.util.Base64;
import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.gateway.dto.inputs.CreateProductInput;
import com.sanedge.pointofsale.gateway.dto.inputs.PageInput;
import com.sanedge.pointofsale.gateway.dto.inputs.ProductCategoryFilterInput;
import com.sanedge.pointofsale.gateway.dto.inputs.ProductMerchantFilterInput;
import com.sanedge.pointofsale.gateway.dto.inputs.UpdateProductInput;
import com.sanedge.pointofsale.gateway.dto.responses.ProductResponse;
import com.sanedge.pointofsale.gateway.dto.responses.ProductResponseDeleteAt;
import com.sanedge.pointofsale.gateway.mapper.ProtoMapper;
import com.sanedge.pointofsale.gateway.service.FileService;
import com.sanedge.pointofsale.gateway.service.FolderService;
import com.sanedge.pointofsale.pb.product.ProductCommandServiceGrpc;
import com.sanedge.pointofsale.pb.product.ProductQueryServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProductResolver {
    private final ProductQueryServiceGrpc.ProductQueryServiceBlockingStub productQueryStub;
    private final ProductCommandServiceGrpc.ProductCommandServiceBlockingStub productCommandStub;
    private final FileService fileService;
    private final FolderService folderService;

    private static final String PRODUCT_BASE_PATH = "static/product";
    private static final long MAX_IMAGE_BYTES = 2 * 1024 * 1024L;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<ProductResponse>> products(@Argument PageInput input) {
        var resp = productQueryStub.findAll(com.sanedge.pointofsale.pb.product.FindAllProductRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<ProductResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.products(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<ProductResponse> product(@Argument Integer id) {
        var resp = productQueryStub.findById(com.sanedge.pointofsale.pb.product.FindByIdProductRequest.newBuilder()
                .setProductId(id)
                .build());
        return ApiResponse.<ProductResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.product(resp.getData()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<ProductResponse>> productsByMerchant(
            @Argument ProductMerchantFilterInput input) {
        var resp = productQueryStub.findByMerchant(
                com.sanedge.pointofsale.pb.product.FindByMerchantProductRequest.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setSearch(nvl(input != null ? input.search() : null))
                        .setCategoryId(nvl(input != null ? input.categoryId() : null))
                        .setMinPrice(nvl(input != null ? input.minPrice() : null))
                        .setMaxPrice(nvl(input != null ? input.maxPrice() : null))
                        .setPage(nvl(input != null ? input.page() : null))
                        .setPageSize(nvl(input != null ? input.pageSize() : null))
                        .build());
        return ApiResponsePagination.<List<ProductResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.products(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<ProductResponse>> productsByCategory(
            @Argument ProductCategoryFilterInput input) {
        var resp = productQueryStub.findByCategory(
                com.sanedge.pointofsale.pb.product.FindByCategoryProductRequest.newBuilder()
                        .setCategoryName(nvl(input != null ? input.categoryName() : null))
                        .setSearch(nvl(input != null ? input.search() : null))
                        .setMinPrice(nvl(input != null ? input.minPrice() : null))
                        .setMaxPrice(nvl(input != null ? input.maxPrice() : null))
                        .setPage(nvl(input != null ? input.page() : null))
                        .setPageSize(nvl(input != null ? input.pageSize() : null))
                        .build());
        return ApiResponsePagination.<List<ProductResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.products(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<ProductResponseDeleteAt>> productsActive(@Argument PageInput input) {
        var resp = productQueryStub.findByActive(com.sanedge.pointofsale.pb.product.FindAllProductRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<ProductResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.productsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<ProductResponseDeleteAt>> productsTrashed(@Argument PageInput input) {
        var resp = productQueryStub.findByTrashed(com.sanedge.pointofsale.pb.product.FindAllProductRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<ProductResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.productsDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<ProductResponse> createProduct(@Argument CreateProductInput input) {
        String savedPath = saveImage(input.imageProductBase64(), input.imageProductFilename(), input.slugProduct());
        if (savedPath == null) {
            return ApiResponse.<ProductResponse>builder()
                    .status("error")
                    .message("Failed to save product image")
                    .data(null)
                    .build();
        }

        var resp = productCommandStub.createProduct(
                com.sanedge.pointofsale.pb.product.CreateProductRequest.newBuilder()
                        .setMerchantId(nvl(input.merchantId()))
                        .setCategoryId(nvl(input.categoryId()))
                        .setName(nvl(input.name()))
                        .setDescription(nvl(input.description()))
                        .setPrice(nvl(input.price()))
                        .setCountInStock(nvl(input.countInStock()))
                        .setBrand(nvl(input.brand()))
                        .setWeight(String.valueOf(nvl(input.weight())))
                        .setRating(nvl(input.rating()))
                        .setSlugProduct(nvl(input.slugProduct()))
                        .setImageProduct(savedPath)
                        .build());
        return ApiResponse.<ProductResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.product(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<ProductResponse> updateProduct(@Argument UpdateProductInput input) {
        String imageProduct = "";
        String savedPath = saveImage(input.imageProductBase64(), input.imageProductFilename(), input.slugProduct());
        if (savedPath != null) {
            imageProduct = savedPath;
        }

        var resp = productCommandStub.updateProduct(
                com.sanedge.pointofsale.pb.product.UpdateProductRequest.newBuilder()
                        .setProductId(nvl(input.productId()))
                        .setMerchantId(nvl(input.merchantId()))
                        .setCategoryId(nvl(input.categoryId()))
                        .setName(nvl(input.name()))
                        .setDescription(nvl(input.description()))
                        .setPrice(nvl(input.price()))
                        .setCountInStock(nvl(input.countInStock()))
                        .setBrand(nvl(input.brand()))
                        .setWeight(String.valueOf(nvl(input.weight())))
                        .setRating(nvl(input.rating()))
                        .setSlugProduct(nvl(input.slugProduct()))
                        .setImageProduct(imageProduct)
                        .build());
        return ApiResponse.<ProductResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.product(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<ProductResponseDeleteAt> trashedProduct(@Argument Integer id) {
        var resp = productCommandStub.trashedProduct(
                com.sanedge.pointofsale.pb.product.FindByIdProductRequest.newBuilder()
                        .setProductId(id)
                        .build());
        return ApiResponse.<ProductResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.productDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<ProductResponseDeleteAt> restoreProduct(@Argument Integer id) {
        var resp = productCommandStub.restoreProduct(
                com.sanedge.pointofsale.pb.product.FindByIdProductRequest.newBuilder()
                        .setProductId(id)
                        .build());
        return ApiResponse.<ProductResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.productDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteProductPermanent(@Argument Integer id) {
        var resp = productCommandStub.deleteProductPermanent(
                com.sanedge.pointofsale.pb.product.FindByIdProductRequest.newBuilder()
                        .setProductId(id)
                        .build());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> restoreAllProducts() {
        var resp = productCommandStub.restoreAllProduct(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteAllProductsPermanent() {
        var resp = productCommandStub.deleteAllProductPermanent(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    /**
     * Decodes an optional Base64 image payload and persists it under the product
     * folder. Returns {@code null} when no image was supplied or the write failed;
     * callers must distinguish "no image" (leave unchanged) from "failed".
     */
    private String saveImage(String base64, String filename, String slugProduct) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (bytes.length > MAX_IMAGE_BYTES) {
            return null;
        }
        String folderPath = folderService.createFolder(PRODUCT_BASE_PATH, nvl(slugProduct));
        if (folderPath == null) {
            return null;
        }
        String safeName = (filename == null || filename.isBlank()) ? "product-image" : filename;
        String filePath = folderPath + File.separator + safeName;
        return fileService.createFileImage(bytes, filePath);
    }

    private static int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
