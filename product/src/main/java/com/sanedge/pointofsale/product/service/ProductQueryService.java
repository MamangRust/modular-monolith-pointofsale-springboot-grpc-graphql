package com.sanedge.pointofsale.product.service;

import java.util.List;

import com.sanedge.pointofsale.product.domain.requests.FindAllProductByCategoryRequest;
import com.sanedge.pointofsale.product.domain.requests.FindAllProductByMerchantRequest;
import com.sanedge.pointofsale.product.domain.requests.FindAllProductRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.product.domain.responses.ProductResponse;
import com.sanedge.pointofsale.product.domain.responses.ProductResponseDeleteAt;

public interface ProductQueryService {
    ApiResponsePagination<List<ProductResponse>> findAll(FindAllProductRequest req);

    ApiResponsePagination<List<ProductResponseDeleteAt>> findActiveProducts(FindAllProductRequest req);

    ApiResponsePagination<List<ProductResponseDeleteAt>> findTrashedProducts(FindAllProductRequest req);

    ApiResponsePagination<List<ProductResponse>> findByMerchant(FindAllProductByMerchantRequest req);

    ApiResponsePagination<List<ProductResponse>> findByCategoryName(FindAllProductByCategoryRequest req);

    ApiResponse<ProductResponse> findById(Long productId);

}
