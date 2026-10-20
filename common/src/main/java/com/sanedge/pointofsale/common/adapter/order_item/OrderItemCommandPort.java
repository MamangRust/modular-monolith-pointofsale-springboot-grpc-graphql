package com.sanedge.pointofsale.common.adapter.order_item;

import com.sanedge.pointofsale.common.adapter.model.CreateOrderItemCmd;
import com.sanedge.pointofsale.common.adapter.model.OrderItem;
import com.sanedge.pointofsale.common.adapter.model.UpdateOrderItemCmd;

/**
 * Port for the order_item command service, replacing direct
 * {@code @GrpcClient("order_item")} usage in consumer services.
 */
public interface OrderItemCommandPort {

    OrderItem createOrderItem(CreateOrderItemCmd cmd);

    OrderItem updateOrderItem(UpdateOrderItemCmd cmd);
}
