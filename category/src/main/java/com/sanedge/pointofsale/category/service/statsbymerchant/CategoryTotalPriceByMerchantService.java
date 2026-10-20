package com.sanedge.pointofsale.category.service.statsbymerchant;

import java.util.List;

import com.sanedge.pointofsale.category.domain.requests.MonthTotalPriceMerchant;
import com.sanedge.pointofsale.category.domain.requests.YearTotalPriceMerchant;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesMonthlyTotalPriceResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesYearlyTotalPriceResponse;

public interface CategoryTotalPriceByMerchantService {
    ApiResponse<List<CategoriesMonthlyTotalPriceResponse>> findMonthlyTotalPriceByMerchant(MonthTotalPriceMerchant req);

    ApiResponse<List<CategoriesYearlyTotalPriceResponse>> findYearlyTotalPriceByMerchant(YearTotalPriceMerchant req);
}
