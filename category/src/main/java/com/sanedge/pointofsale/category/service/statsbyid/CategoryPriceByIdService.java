package com.sanedge.pointofsale.category.service.statsbyid;

import java.util.List;

import com.sanedge.pointofsale.category.domain.requests.MonthPriceId;
import com.sanedge.pointofsale.category.domain.requests.YearPriceId;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesMonthPriceResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesYearPriceResponse;

public interface CategoryPriceByIdService {
    ApiResponse<List<CategoriesMonthPriceResponse>> findMonthPriceById(MonthPriceId req);

    ApiResponse<List<CategoriesYearPriceResponse>> findYearPriceById(YearPriceId req);
}
