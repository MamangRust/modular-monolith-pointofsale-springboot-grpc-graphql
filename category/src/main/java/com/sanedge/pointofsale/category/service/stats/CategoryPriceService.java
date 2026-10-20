package com.sanedge.pointofsale.category.service.stats;

import java.util.List;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesMonthPriceResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesYearPriceResponse;

public interface CategoryPriceService {
    ApiResponse<List<CategoriesMonthPriceResponse>> findMonthPrice(Integer year);

    ApiResponse<List<CategoriesYearPriceResponse>> findYearPrice(Integer year);
}
