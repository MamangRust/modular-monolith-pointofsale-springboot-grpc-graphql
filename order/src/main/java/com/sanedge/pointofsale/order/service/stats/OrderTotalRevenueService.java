package com.sanedge.pointofsale.order.service.stats;

import java.util.List;

import com.sanedge.pointofsale.order.domain.requests.MonthTotalRevenue;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderMonthlyTotalRevenueResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderYearlyTotalRevenueResponse;

public interface OrderTotalRevenueService {
    ApiResponse<List<OrderMonthlyTotalRevenueResponse>> findMonthlyStats(MonthTotalRevenue req);

    ApiResponse<List<OrderYearlyTotalRevenueResponse>> findYearlyStats(Integer year);
}