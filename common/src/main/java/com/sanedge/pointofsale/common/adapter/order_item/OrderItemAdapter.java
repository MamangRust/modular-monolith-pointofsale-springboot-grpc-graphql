package com.sanedge.pointofsale.common.adapter.order_item;

import java.util.ArrayList;
import java.util.List;

import com.sanedge.pointofsale.common.adapter.model.CreateOrderItemCmd;
import com.sanedge.pointofsale.common.adapter.model.OrderItem;
import com.sanedge.pointofsale.common.adapter.model.UpdateOrderItemCmd;
import com.sanedge.pointofsale.common.adapter.support.ProtoTime;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.order_item.CreateOrderItemRequest;
import com.sanedge.pointofsale.pb.order_item.FindByOrderOrderItemRequest;
import com.sanedge.pointofsale.pb.order_item.OrderItemCommandServiceGrpc.OrderItemCommandServiceBlockingStub;
import com.sanedge.pointofsale.pb.order_item.OrderItemQueryServiceGrpc.OrderItemQueryServiceBlockingStub;
import com.sanedge.pointofsale.pb.order_item.OrderItemResponse;
import com.sanedge.pointofsale.pb.order_item.UpdateOrderItemRequest;

public class OrderItemAdapter implements OrderItemQueryPort, OrderItemCommandPort {

    private final OrderItemQueryServiceBlockingStub query;

    private final OrderItemCommandServiceBlockingStub command;

    public OrderItemAdapter(OrderItemQueryServiceBlockingStub query, OrderItemCommandServiceBlockingStub command) {
        this.query = query;
        this.command = command;
    }

    @Override
    public List<OrderItem> findOrderItemByOrder(int orderId) {
        var resp = query.findByOrder(FindByOrderOrderItemRequest.newBuilder().setOrderId(orderId).build());
        List<OrderItem> result = new ArrayList<>();
        if (resp != null && resp.getDataList() != null) {
            for (OrderItemResponse item : resp.getDataList()) {
                OrderItem mapped = toOrderItem(item);
                if (mapped != null) {
                    result.add(mapped);
                }
            }
        }
        return result;
    }

    @Override
    public OrderItem createOrderItem(CreateOrderItemCmd cmd) {
        var resp = command.createOrderItem(CreateOrderItemRequest.newBuilder()
                .setOrderId(cmd.orderId())
                .setProductId(cmd.productId())
                .setQuantity(cmd.quantity())
                .setPrice(cmd.price())
                .build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Failed to create order item");
        }
        return toOrderItem(resp.getData());
    }

    @Override
    public OrderItem updateOrderItem(UpdateOrderItemCmd cmd) {
        var resp = command.updateOrderItem(UpdateOrderItemRequest.newBuilder()
                .setOrderItemId(cmd.orderItemId())
                .setOrderId(cmd.orderId())
                .setProductId(cmd.productId())
                .setQuantity(cmd.quantity())
                .setPrice(cmd.price())
                .build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Failed to update order item");
        }
        return toOrderItem(resp.getData());
    }

    private static OrderItem toOrderItem(OrderItemResponse i) {
        if (i == null) {
            return null;
        }
        return new OrderItem(i.getOrderItemId(), i.getOrderId(), i.getProductId(), i.getQuantity(), i.getPrice(),
                ProtoTime.parse(i.getCreatedAt()), ProtoTime.parse(i.getUpdatedAt()));
    }
}
