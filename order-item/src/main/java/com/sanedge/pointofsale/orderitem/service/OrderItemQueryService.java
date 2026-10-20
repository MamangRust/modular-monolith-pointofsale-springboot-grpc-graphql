package com.sanedge.pointofsale.orderitem.service;

import java.util.List;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.orderitem.domain.responses.OrderItemResponse;

public interface OrderItemQueryService {
    ApiResponse<List<OrderItemResponse>> findByOrder(Integer orderId);

    ApiResponse<OrderItemResponse> findById(Integer id);
}
