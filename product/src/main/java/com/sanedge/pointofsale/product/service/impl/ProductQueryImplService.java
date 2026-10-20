package com.sanedge.pointofsale.product.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.product.domain.requests.FindAllProductByCategoryRequest;
import com.sanedge.pointofsale.product.domain.requests.FindAllProductByMerchantRequest;
import com.sanedge.pointofsale.product.domain.requests.FindAllProductRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.domain.responses.api.PaginationMeta;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.pb.category.ApiResponsePaginationCategory;
import com.sanedge.pointofsale.pb.category.CategoryQueryServiceGrpc;
import com.sanedge.pointofsale.pb.category.CategoryResponse;
import com.sanedge.pointofsale.pb.category.FindAllCategoryRequest;
import com.sanedge.pointofsale.product.domain.responses.ProductResponse;
import com.sanedge.pointofsale.product.domain.responses.ProductResponseDeleteAt;
import com.sanedge.pointofsale.product.models.Product;
import com.sanedge.pointofsale.product.repository.ProductQueryRepository;
import com.sanedge.pointofsale.product.service.ProductQueryService;

import io.grpc.StatusRuntimeException;
import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQueryImplService implements ProductQueryService {
        private final ProductQueryRepository productQueryRepository;
        private final CategoryQueryServiceGrpc.CategoryQueryServiceBlockingStub categoryQueryStub;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<ProductResponse>> findAll(FindAllProductRequest req) {
                return tracingMetrics.traceAndMeasure("findAllProducts", "find_all_products", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Fetching products | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        Pageable pageable = PageRequest.of(page, pageSize);
                        Page<Product> productPage = productQueryRepository.findAllProducts(keyword, pageable);

                        List<ProductResponse> responses = productPage.getContent()
                                        .stream()
                                        .map(ProductResponse::from)
                                        .toList();

                        log.info("✅ Found {} products", responses.size());

                        return ApiResponsePagination.<List<ProductResponse>>builder()
                                        .status("success")
                                        .message("Products retrieved successfully")
                                        .data(responses)
                                        .pagination(PaginationMeta.fromSpringPage(productPage))
                                        .build();
                });
        }

        @Override
        public ApiResponsePagination<List<ProductResponseDeleteAt>> findActiveProducts(FindAllProductRequest req) {
                return tracingMetrics.traceAndMeasure("findActiveProducts", "find_active_products", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Fetching ACTIVE products | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Product> productPage = productQueryRepository.findActiveProducts(keyword, pageable);

                                List<ProductResponseDeleteAt> responses = productPage.getContent()
                                                .stream()
                                                .map(ProductResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} active products", responses.size());

                                return ApiResponsePagination.<List<ProductResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Active products retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(productPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch active products", e);
                                return ApiResponsePagination.<List<ProductResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to fetch active products")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<ProductResponseDeleteAt>> findTrashedProducts(FindAllProductRequest req) {
                return tracingMetrics.traceAndMeasure("findTrashedProducts", "find_trashed_products", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Fetching TRASHED products | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Product> productPage = productQueryRepository.findTrashedProducts(keyword, pageable);

                                List<ProductResponseDeleteAt> responses = productPage.getContent()
                                                .stream()
                                                .map(ProductResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} trashed products", responses.size());

                                return ApiResponsePagination.<List<ProductResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Trashed products retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(productPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch trashed products", e);
                                return ApiResponsePagination.<List<ProductResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to fetch trashed products")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<ProductResponse>> findByMerchant(FindAllProductByMerchantRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("merchant.id", req.getMerchantId() != null ? req.getMerchantId() : 0)
                                .build();

                return tracingMetrics.traceAndMeasure("findProductsByMerchant", "find_products_by_merchant", attrs, () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Fetching products by merchant | MerchantID: {}, Page: {}, Size: {}, Search: {}",
                                        req.getMerchantId(), page + 1, pageSize, keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);

                                Page<Product> productPage = productQueryRepository.findProductsByMerchant(
                                                req.getMerchantId().longValue(),
                                                keyword,
                                                req.getCategoryId().longValue(),
                                                req.getMinPrice(),
                                                req.getMaxPrice(),
                                                pageable);

                                List<ProductResponse> responses = productPage.getContent()
                                                .stream()
                                                .map(ProductResponse::from)
                                                .toList();

                                log.info("✅ Found {} products for merchant {}", responses.size(), req.getMerchantId());

                                return ApiResponsePagination.<List<ProductResponse>>builder()
                                                .status("success")
                                                .message("Products by merchant retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(productPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch products for merchant {}", req.getMerchantId(), e);
                                return ApiResponsePagination.<List<ProductResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch products by merchant")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<ProductResponse>> findByCategoryName(FindAllProductByCategoryRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("category.name", req.getCategoryName() != null ? req.getCategoryName() : "")
                                .build();

                return tracingMetrics.traceAndMeasure("findProductsByCategoryName", "find_products_by_category_name",
                                attrs, () -> {
                                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty())
                                                        ? req.getSearch()
                                                        : "";

                                        log.info("🔍 Fetching products by category | Category: {}, Page: {}, Size: {}, Search: {}",
                                                        req.getCategoryName(), page + 1, pageSize,
                                                        keyword.isEmpty() ? "None" : keyword);

                                        try {
                                                Pageable pageable = PageRequest.of(page, pageSize);

                                                Long categoryId = resolveCategoryId(req.getCategoryName());
                                                if (categoryId == null) {
                                                        log.warn("❌ Category not found with name: {}",
                                                                        req.getCategoryName());
                                                        return ApiResponsePagination.<List<ProductResponse>>builder()
                                                                        .status("success")
                                                                        .message("Products by category retrieved successfully")
                                                                        .data(Collections.emptyList())
                                                                        .pagination(null)
                                                                        .build();
                                                }

                                                Page<Product> productPage = productQueryRepository.findProductsByCategoryId(
                                                                categoryId,
                                                                keyword,
                                                                req.getMinPrice(),
                                                                req.getMaxPrice(),
                                                                pageable);

                                                List<ProductResponse> responses = productPage.getContent()
                                                                .stream()
                                                                .map(ProductResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} products for category {}", responses.size(),
                                                                req.getCategoryName());

                                                return ApiResponsePagination.<List<ProductResponse>>builder()
                                                                .status("success")
                                                                .message("Products by category retrieved successfully")
                                                                .data(responses)
                                                                .pagination(PaginationMeta.fromSpringPage(productPage))
                                                                .build();
                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch products for category {}", req.getCategoryName(), e);
                                                return ApiResponsePagination.<List<ProductResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to fetch products by category")
                                                                .data(Collections.emptyList())
                                                                .pagination(null)
                                                                .build();
                                        }
                                });
        }

        private Long resolveCategoryId(String categoryName) {
                if (categoryName == null || categoryName.isBlank()) {
                        return null;
                }
                try {
                        ApiResponsePaginationCategory res = categoryQueryStub.findAll(
                                        FindAllCategoryRequest.newBuilder()
                                                        .setPage(1)
                                                        .setPageSize(100)
                                                        .setSearch(categoryName)
                                                        .build());
                        for (CategoryResponse category : res.getDataList()) {
                                if (category.getName().equalsIgnoreCase(categoryName)) {
                                        return (long) category.getCategoryId();
                                }
                        }
                } catch (StatusRuntimeException e) {
                        log.warn("Category lookup failed for {}: {}", categoryName, e.getStatus().getCode());
                }
                return null;
        }

        @Override
        public ApiResponse<ProductResponse> findById(Long productId) {
                Attributes attrs = Attributes.builder()
                                .put("product.id", productId != null ? productId : 0L)
                                .build();

                return tracingMetrics.traceAndMeasure("findProductById", "find_product_by_id", attrs, () -> {
                        log.info("🔍 Fetching product by ID: {}", productId);
                        try {
                                return productQueryRepository.findById(productId)
                                                .map(product -> ApiResponse.<ProductResponse>builder()
                                                                .status("success")
                                                                .message("Product retrieved successfully")
                                                                .data(ProductResponse.from(product))
                                                                .build())
                                                .orElseGet(() -> {
                                                        log.warn("❌ Product not found with ID: {}", productId);
                                                        return ApiResponse.<ProductResponse>builder()
                                                                        .status("error")
                                                                        .message("Product not found")
                                                                        .data(null)
                                                                        .build();
                                                });
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch product by ID: {}", productId, e);
                                return ApiResponse.<ProductResponse>builder()
                                                .status("error")
                                                .message("Failed to fetch product")
                                                .data(null)
                                                .build();
                        }
                });
        }

}
