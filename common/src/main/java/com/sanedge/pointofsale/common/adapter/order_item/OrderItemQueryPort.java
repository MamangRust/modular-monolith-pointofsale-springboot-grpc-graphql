package com.sanedge.pointofsale.common.adapter.order_item;

import java.util.List;

import com.sanedge.pointofsale.common.adapter.model.OrderItem;

/**
 * Port for the order_item query service, replacing direct
 * {@code @GrpcClient("order_item")} usage in consumer services.
 */
public interface OrderItemQueryPort {

    List<OrderItem> findOrderItemByOrder(int orderId);
}
