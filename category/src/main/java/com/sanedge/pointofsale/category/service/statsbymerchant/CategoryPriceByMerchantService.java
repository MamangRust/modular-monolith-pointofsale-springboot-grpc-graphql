package com.sanedge.pointofsale.category.service.statsbymerchant;

import java.util.List;

import com.sanedge.pointofsale.category.domain.requests.MonthPriceMerchant;
import com.sanedge.pointofsale.category.domain.requests.YearPriceMerchant;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesMonthPriceResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesYearPriceResponse;

public interface CategoryPriceByMerchantService {
    ApiResponse<List<CategoriesMonthPriceResponse>> findMonthPriceByMerchant(MonthPriceMerchant req);

    ApiResponse<List<CategoriesYearPriceResponse>> findYearPriceByMerchant(YearPriceMerchant req);
}
