package com.sanedge.pointofsale.order.service.stats;

import java.util.List;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderMonthlyResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderYearlyResponse;

public interface OrderSoldoutService {
    ApiResponse<List<OrderMonthlyResponse>> findMonthlyOrders(Integer yearMonth);

    ApiResponse<List<OrderYearlyResponse>> findYearlyOrders(Integer year);
}
