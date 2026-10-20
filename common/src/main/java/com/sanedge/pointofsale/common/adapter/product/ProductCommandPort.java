package com.sanedge.pointofsale.common.adapter.product;

import com.sanedge.pointofsale.common.adapter.model.Product;
import com.sanedge.pointofsale.common.adapter.model.UpdateProductCmd;

/**
 * Port for the product command service, replacing direct
 * {@code @GrpcClient("product")} usage in consumer services.
 */
public interface ProductCommandPort {

    Product update(UpdateProductCmd cmd);
}
