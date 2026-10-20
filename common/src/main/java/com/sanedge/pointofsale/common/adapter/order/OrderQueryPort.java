package com.sanedge.pointofsale.common.adapter.order;

import com.sanedge.pointofsale.common.adapter.model.Order;

/**
 * Port for the order query service, replacing direct
 * {@code @GrpcClient("order")} usage in consumer services.
 */
public interface OrderQueryPort {

    Order findById(int orderId);
}
