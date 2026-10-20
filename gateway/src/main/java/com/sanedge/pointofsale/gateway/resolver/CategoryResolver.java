package com.sanedge.pointofsale.gateway.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.gateway.dto.inputs.CategoryMerchantMonthTotalPriceInput;
import com.sanedge.pointofsale.gateway.dto.inputs.CategoryMerchantYearTotalPriceInput;
import com.sanedge.pointofsale.gateway.dto.inputs.CategoryMonthTotalPriceInput;
import com.sanedge.pointofsale.gateway.dto.inputs.CategoryYearTotalPriceInput;
import com.sanedge.pointofsale.gateway.dto.inputs.CreateCategoryInput;
import com.sanedge.pointofsale.gateway.dto.inputs.MonthTotalPriceInput;
import com.sanedge.pointofsale.gateway.dto.inputs.PageInput;
import com.sanedge.pointofsale.gateway.dto.inputs.UpdateCategoryInput;
import com.sanedge.pointofsale.gateway.dto.inputs.YearPriceInput;
import com.sanedge.pointofsale.gateway.dto.responses.CategoriesMonthPriceResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CategoriesMonthlyTotalPriceResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CategoriesYearPriceResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CategoriesYearlyTotalPriceResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CategoryResponse;
import com.sanedge.pointofsale.gateway.dto.responses.CategoryResponseDeleteAt;
import com.sanedge.pointofsale.gateway.mapper.ProtoMapper;
import com.sanedge.pointofsale.pb.category.CategoryCommandServiceGrpc;
import com.sanedge.pointofsale.pb.category.CategoryQueryServiceGrpc;
import com.sanedge.pointofsale.pb.category.stats.CategoryPriceServiceGrpc;
import com.sanedge.pointofsale.pb.category.stats.CategoryTotalPriceServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CategoryResolver {
    private final CategoryQueryServiceGrpc.CategoryQueryServiceBlockingStub categoryQueryStub;
    private final CategoryCommandServiceGrpc.CategoryCommandServiceBlockingStub categoryCommandStub;
    private final CategoryTotalPriceServiceGrpc.CategoryTotalPriceServiceBlockingStub categoryTotalPriceStub;
    private final CategoryPriceServiceGrpc.CategoryPriceServiceBlockingStub categoryPriceStub;

    @QueryMapping
    public ApiResponsePagination<List<CategoryResponse>> categories(@Argument PageInput input) {
        var resp = categoryQueryStub.findAll(com.sanedge.pointofsale.pb.category.FindAllCategoryRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<CategoryResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.categories(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    public ApiResponsePagination<List<CategoryResponseDeleteAt>> categoriesActive(@Argument PageInput input) {
        var resp = categoryQueryStub.findByActive(com.sanedge.pointofsale.pb.category.FindAllCategoryRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<CategoryResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.categoriesDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    public ApiResponsePagination<List<CategoryResponseDeleteAt>> categoriesTrashed(@Argument PageInput input) {
        var resp = categoryQueryStub.findByTrashed(com.sanedge.pointofsale.pb.category.FindAllCategoryRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<CategoryResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.categoriesDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    public ApiResponse<CategoryResponse> category(@Argument Integer id) {
        var resp = categoryQueryStub.findById(com.sanedge.pointofsale.pb.category.FindByIdCategoryRequest.newBuilder()
                .setCategoryId(id)
                .build());
        return ApiResponse.<CategoryResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.category(resp.getData()))
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesMonthlyTotalPriceResponse>> categoryMonthTotalPrice(
            @Argument MonthTotalPriceInput input) {
        var resp = categoryTotalPriceStub.findMonthlyTotalPrices(
                com.sanedge.pointofsale.pb.category.FindYearMonthTotalPrice.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<CategoriesMonthlyTotalPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryMonthTotalPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesYearlyTotalPriceResponse>> categoryYearTotalPrice(
            @Argument YearPriceInput input) {
        var resp = categoryTotalPriceStub.findYearlyTotalPrices(
                com.sanedge.pointofsale.pb.category.FindYearTotalPrice.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CategoriesYearlyTotalPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryYearTotalPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesMonthlyTotalPriceResponse>> categoryMerchantMonthTotalPrice(
            @Argument CategoryMerchantMonthTotalPriceInput input) {
        var resp = categoryTotalPriceStub.findMonthlyTotalPricesByMerchant(
                com.sanedge.pointofsale.pb.category.FindYearMonthTotalPriceByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<CategoriesMonthlyTotalPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryMonthTotalPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesYearlyTotalPriceResponse>> categoryMerchantYearTotalPrice(
            @Argument CategoryMerchantYearTotalPriceInput input) {
        var resp = categoryTotalPriceStub.findYearlyTotalPricesByMerchant(
                com.sanedge.pointofsale.pb.category.FindYearTotalPriceByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CategoriesYearlyTotalPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryYearTotalPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesMonthlyTotalPriceResponse>> categoryMyMonthTotalPrice(
            @Argument CategoryMonthTotalPriceInput input) {
        var resp = categoryTotalPriceStub.findMonthlyTotalPricesById(
                com.sanedge.pointofsale.pb.category.FindYearMonthTotalPriceById.newBuilder()
                        .setCategoryId(nvl(input != null ? input.categoryId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<CategoriesMonthlyTotalPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryMonthTotalPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesYearlyTotalPriceResponse>> categoryMyYearTotalPrice(
            @Argument CategoryYearTotalPriceInput input) {
        var resp = categoryTotalPriceStub.findYearlyTotalPricesById(
                com.sanedge.pointofsale.pb.category.FindYearTotalPriceById.newBuilder()
                        .setCategoryId(nvl(input != null ? input.categoryId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CategoriesYearlyTotalPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryYearTotalPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesMonthPriceResponse>> categoryMonthPrice(@Argument YearPriceInput input) {
        var resp = categoryPriceStub.findMonthPrice(com.sanedge.pointofsale.pb.category.FindYearCategory.newBuilder()
                .setYear(nvl(input != null ? input.year() : null))
                .build());
        return ApiResponse.<List<CategoriesMonthPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryMonthPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesYearPriceResponse>> categoryYearPrice(@Argument YearPriceInput input) {
        var resp = categoryPriceStub.findYearPrice(com.sanedge.pointofsale.pb.category.FindYearCategory.newBuilder()
                .setYear(nvl(input != null ? input.year() : null))
                .build());
        return ApiResponse.<List<CategoriesYearPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryYearPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesMonthPriceResponse>> categoryMerchantMonthPrice(
            @Argument CategoryMerchantYearTotalPriceInput input) {
        var resp = categoryPriceStub.findMonthPriceByMerchant(
                com.sanedge.pointofsale.pb.category.FindYearCategoryByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CategoriesMonthPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryMonthPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesYearPriceResponse>> categoryMerchantYearPrice(
            @Argument CategoryMerchantYearTotalPriceInput input) {
        var resp = categoryPriceStub.findYearPriceByMerchant(
                com.sanedge.pointofsale.pb.category.FindYearCategoryByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CategoriesYearPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryYearPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesMonthPriceResponse>> categoryMyMonthPrice(
            @Argument CategoryYearTotalPriceInput input) {
        var resp = categoryPriceStub.findMonthPriceById(
                com.sanedge.pointofsale.pb.category.FindYearCategoryById.newBuilder()
                        .setCategoryId(nvl(input != null ? input.categoryId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CategoriesMonthPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryMonthPrice).toList())
                .build();
    }

    @QueryMapping
    public ApiResponse<List<CategoriesYearPriceResponse>> categoryMyYearPrice(
            @Argument CategoryYearTotalPriceInput input) {
        var resp = categoryPriceStub.findYearPriceById(
                com.sanedge.pointofsale.pb.category.FindYearCategoryById.newBuilder()
                        .setCategoryId(nvl(input != null ? input.categoryId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<CategoriesYearPriceResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::categoryYearPrice).toList())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<CategoryResponse> createCategory(@Argument CreateCategoryInput input) {
        var resp = categoryCommandStub.createCategory(
                com.sanedge.pointofsale.pb.category.CreateCategoryRequest.newBuilder()
                        .setName(nvl(input.name()))
                        .setDescription(nvl(input.description()))
                        .setSlugCategory(nvl(input.slugCategory()))
                        .build());
        return ApiResponse.<CategoryResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.category(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<CategoryResponse> updateCategory(@Argument UpdateCategoryInput input) {
        var resp = categoryCommandStub.updateCategory(
                com.sanedge.pointofsale.pb.category.UpdateCategoryRequest.newBuilder()
                        .setCategoryId(nvl(input.categoryId()))
                        .setName(nvl(input.name()))
                        .setDescription(nvl(input.description()))
                        .setSlugCategory(nvl(input.slugCategory()))
                        .build());
        return ApiResponse.<CategoryResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.category(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<CategoryResponseDeleteAt> trashedCategory(@Argument Integer id) {
        var resp = categoryCommandStub.trashedCategory(
                com.sanedge.pointofsale.pb.category.FindByIdCategoryRequest.newBuilder()
                        .setCategoryId(id)
                        .build());
        return ApiResponse.<CategoryResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.categoryDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<CategoryResponseDeleteAt> restoreCategory(@Argument Integer id) {
        var resp = categoryCommandStub.restoreCategory(
                com.sanedge.pointofsale.pb.category.FindByIdCategoryRequest.newBuilder()
                        .setCategoryId(id)
                        .build());
        return ApiResponse.<CategoryResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.categoryDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteCategoryPermanent(@Argument Integer id) {
        var resp = categoryCommandStub.deleteCategoryPermanent(
                com.sanedge.pointofsale.pb.category.FindByIdCategoryRequest.newBuilder()
                        .setCategoryId(id)
                        .build());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> restoreAllCategories() {
        var resp = categoryCommandStub.restoreAllCategory(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteAllCategoriesPermanent() {
        var resp = categoryCommandStub.deleteAllCategoryPermanent(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    private static int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
