package com.sanedge.pointofsale.common.adapter.product;

import com.sanedge.pointofsale.common.adapter.model.Product;

/**
 * Port for the product query service, replacing direct
 * {@code @GrpcClient("product")} usage in consumer services.
 */
public interface ProductQueryPort {

    Product findById(int productId);
}
