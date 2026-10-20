package com.sanedge.pointofsale.category.service.statsbyid;

import java.util.List;

import com.sanedge.pointofsale.category.domain.requests.MonthTotalPriceCategory;
import com.sanedge.pointofsale.category.domain.requests.YearTotalPriceCategory;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesMonthlyTotalPriceResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesYearlyTotalPriceResponse;

public interface CategoryTotalPriceByIdService {
    ApiResponse<List<CategoriesMonthlyTotalPriceResponse>> findMonthlyTotalPriceById(MonthTotalPriceCategory req);

    ApiResponse<List<CategoriesYearlyTotalPriceResponse>> findYearlyTotalPriceById(YearTotalPriceCategory req);
}
