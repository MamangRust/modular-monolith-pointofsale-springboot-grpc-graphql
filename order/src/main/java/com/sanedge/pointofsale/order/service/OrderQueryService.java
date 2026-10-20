package com.sanedge.pointofsale.order.service;

import java.util.List;

import com.sanedge.pointofsale.order.domain.requests.FindAllOrderByMerchantRequest;
import com.sanedge.pointofsale.order.domain.requests.FindAllOrderRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.order.domain.responses.OrderResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt;

public interface OrderQueryService {
    ApiResponsePagination<List<OrderResponse>> findAll(FindAllOrderRequest req);

    ApiResponsePagination<List<OrderResponseDeleteAt>> findByActive(FindAllOrderRequest req);

    ApiResponsePagination<List<OrderResponseDeleteAt>> findByTrashed(FindAllOrderRequest req);

    ApiResponse<OrderResponse> findById(Integer id);

    ApiResponsePagination<List<OrderResponse>> findByMerchantId(FindAllOrderByMerchantRequest req);
}
