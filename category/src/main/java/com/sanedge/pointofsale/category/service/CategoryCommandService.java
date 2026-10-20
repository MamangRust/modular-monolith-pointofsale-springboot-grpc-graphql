package com.sanedge.pointofsale.category.service;

import com.sanedge.pointofsale.category.domain.requests.CreateCategoryRequest;
import com.sanedge.pointofsale.category.domain.requests.UpdateCategoryRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoryResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoryResponseDeleteAt;

public interface CategoryCommandService {
    ApiResponse<CategoryResponse> createCategory(CreateCategoryRequest req);

    ApiResponse<CategoryResponse> updateCategory(UpdateCategoryRequest req);

    ApiResponse<CategoryResponseDeleteAt> trashedCategory(Integer categoryId);

    ApiResponse<CategoryResponseDeleteAt> restoreCategory(Integer categoryId);

    ApiResponse<Boolean> deleteCategoryPermanent(Integer categoryId);

    ApiResponse<Boolean> restoreAllCategories();

    ApiResponse<Boolean> deleteAllCategoriesPermanent();
}
