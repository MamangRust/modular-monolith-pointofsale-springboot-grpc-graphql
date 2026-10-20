package com.sanedge.pointofsale.category.service;

import java.util.List;

import com.sanedge.pointofsale.category.domain.requests.FindAllCategory;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.category.domain.responses.CategoryResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoryResponseDeleteAt;

public interface CategoryQueryService {
    ApiResponsePagination<List<CategoryResponse>> findAll(FindAllCategory req);

    ApiResponse<CategoryResponse> findById(Integer categoryId);

    ApiResponsePagination<List<CategoryResponseDeleteAt>> findByActive(FindAllCategory req);

    ApiResponsePagination<List<CategoryResponseDeleteAt>> findByTrashed(FindAllCategory req);
}
