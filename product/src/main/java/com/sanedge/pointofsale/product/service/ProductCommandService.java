package com.sanedge.pointofsale.product.service;

import com.sanedge.pointofsale.product.domain.requests.CreateProductRequest;
import com.sanedge.pointofsale.product.domain.requests.UpdateProductRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.product.domain.responses.ProductResponse;
import com.sanedge.pointofsale.product.domain.responses.ProductResponseDeleteAt;

public interface ProductCommandService {
    ApiResponse<ProductResponse> createProduct(CreateProductRequest req);

    ApiResponse<ProductResponse> updateProduct(UpdateProductRequest req);

    ApiResponse<ProductResponseDeleteAt> trashedProduct(Integer productId);

    ApiResponse<ProductResponseDeleteAt> restoreProduct(Integer productId);

    ApiResponse<Boolean> deleteProductPermanent(Integer productId);

    ApiResponse<Boolean> restoreAllProducts();

    ApiResponse<Boolean> deleteAllProductsPermanent();
}
