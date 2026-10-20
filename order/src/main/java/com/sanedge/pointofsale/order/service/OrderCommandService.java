package com.sanedge.pointofsale.order.service;

import com.sanedge.pointofsale.order.domain.requests.CreateOrderRequest;
import com.sanedge.pointofsale.order.domain.requests.UpdateOrderRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt;

public interface OrderCommandService {
    ApiResponse<OrderResponse> create(CreateOrderRequest request);

    ApiResponse<OrderResponse> update(UpdateOrderRequest request);

    ApiResponse<OrderResponseDeleteAt> trash(Integer id);

    ApiResponse<OrderResponseDeleteAt> restore(Integer id);

    ApiResponse<Boolean> delete(Integer id);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();
}
