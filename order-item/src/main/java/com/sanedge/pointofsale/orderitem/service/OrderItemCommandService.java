package com.sanedge.pointofsale.orderitem.service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.orderitem.domain.requests.CreateOrderItemRequest;
import com.sanedge.pointofsale.orderitem.domain.requests.UpdateOrderItemRequest;
import com.sanedge.pointofsale.orderitem.domain.responses.OrderItemResponse;

public interface OrderItemCommandService {
    ApiResponse<OrderItemResponse> create(CreateOrderItemRequest req);

    ApiResponse<OrderItemResponse> update(UpdateOrderItemRequest req);
}
