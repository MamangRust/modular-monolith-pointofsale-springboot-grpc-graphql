package com.sanedge.pointofsale.category.service.stats;

import java.util.List;

import com.sanedge.pointofsale.category.domain.requests.MonthTotalPrice;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesMonthlyTotalPriceResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesYearlyTotalPriceResponse;

public interface CategoryTotalPriceService {
    ApiResponse<List<CategoriesMonthlyTotalPriceResponse>> findMonthlyTotalPrice(MonthTotalPrice req);

    ApiResponse<List<CategoriesYearlyTotalPriceResponse>> findYearlyTotalPrice(Integer year);
}
