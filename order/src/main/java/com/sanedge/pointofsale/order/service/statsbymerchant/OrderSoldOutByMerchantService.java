package com.sanedge.pointofsale.order.service.statsbymerchant;

import java.util.List;

import com.sanedge.pointofsale.order.domain.requests.MonthOrderMerchantRequest;
import com.sanedge.pointofsale.order.domain.requests.YearOrderMerchantRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderMonthlyResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderYearlyResponse;

public interface OrderSoldOutByMerchantService {
    ApiResponse<List<OrderMonthlyResponse>> findMonthlyOrdersByMerchant(MonthOrderMerchantRequest req);

    ApiResponse<List<OrderYearlyResponse>> findYearlyOrdersByMerchant(YearOrderMerchantRequest req);
}