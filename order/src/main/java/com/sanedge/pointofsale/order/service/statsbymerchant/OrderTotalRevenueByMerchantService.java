package com.sanedge.pointofsale.order.service.statsbymerchant;

import java.util.List;

import com.sanedge.pointofsale.order.domain.requests.MonthTotalRevenueMerchantRequest;
import com.sanedge.pointofsale.order.domain.requests.YearTotalRevenueMerchantRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderMonthlyTotalRevenueResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderYearlyTotalRevenueResponse;

public interface OrderTotalRevenueByMerchantService {
    ApiResponse<List<OrderMonthlyTotalRevenueResponse>> findMonthlyStatsByMerchant(
            MonthTotalRevenueMerchantRequest req);

    ApiResponse<List<OrderYearlyTotalRevenueResponse>> findYearlyStatsByMerchant(YearTotalRevenueMerchantRequest req);
}