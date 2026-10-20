package com.sanedge.pointofsale.common.adapter.order;

import com.sanedge.pointofsale.common.adapter.model.Order;
import com.sanedge.pointofsale.common.adapter.support.ProtoTime;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.order.FindByIdOrderRequest;
import com.sanedge.pointofsale.pb.order.OrderQueryServiceGrpc.OrderQueryServiceBlockingStub;
import com.sanedge.pointofsale.pb.order.OrderResponse;

public class OrderAdapter implements OrderQueryPort {

    private final OrderQueryServiceBlockingStub query;

    public OrderAdapter(OrderQueryServiceBlockingStub query) {
        this.query = query;
    }

    @Override
    public Order findById(int orderId) {
        var resp = query.findById(FindByIdOrderRequest.newBuilder().setOrderId(orderId).build());
        if (resp == null || !resp.hasData()) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
        return toOrder(resp.getData());
    }

    private static Order toOrder(OrderResponse o) {
        if (o == null) {
            return null;
        }
        return new Order(o.getOrderId(), o.getMerchantId(), o.getCashierId(), (int) o.getTotalPrice(),
                ProtoTime.parse(o.getCreatedAt()), ProtoTime.parse(o.getUpdatedAt()));
    }
}
