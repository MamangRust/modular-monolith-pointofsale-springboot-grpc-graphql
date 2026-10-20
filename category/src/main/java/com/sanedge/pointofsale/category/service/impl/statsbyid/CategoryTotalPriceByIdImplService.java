package com.sanedge.pointofsale.category.service.impl.statsbyid;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.category.domain.requests.MonthTotalPriceCategory;
import com.sanedge.pointofsale.category.domain.requests.YearTotalPriceCategory;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.category.domain.responses.CategoriesMonthlyTotalPriceResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesYearlyTotalPriceResponse;
import com.sanedge.pointofsale.category.models.CategoryMonthTotalPrice;
import com.sanedge.pointofsale.category.models.CategoryYearTotalPrice;
import com.sanedge.pointofsale.category.repository.statsbyid.CategoryTotalPriceByIdRepository;
import com.sanedge.pointofsale.category.service.statsbyid.CategoryTotalPriceByIdService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryTotalPriceByIdImplService implements CategoryTotalPriceByIdService {
        private final CategoryTotalPriceByIdRepository categoryTotalPriceByIdRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<CategoriesMonthlyTotalPriceResponse>> findMonthlyTotalPriceById(
                        MonthTotalPriceCategory req) {
                Attributes attrs = Attributes.builder()
                                .put("category.id", req.getCategoryId() != null ? req.getCategoryId().toString() : "null")
                                .put("category.year", req.getYear() != null ? req.getYear().toString() : "null")
                                .put("category.month", req.getMonth() != null ? req.getMonth().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyTotalPriceById", "find_monthly_total_price_by_id", attrs,
                                () -> {
                                        log.info("📊 Fetching monthly total price by category | CategoryId: {}, Year: {}, Month: {}",
                                                        req.getCategoryId(), req.getYear(), req.getMonth());

                                        if (req.getCategoryId() == null || req.getYear() == null || req.getMonth() == null) {
                                                log.error("❌ CategoryId, Year or Month is null | req: {}", req);
                                                return ApiResponse.<List<CategoriesMonthlyTotalPriceResponse>>builder()
                                                                .status("error")
                                                                .message("CategoryId, Year and Month must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                LocalDate currentMonth = LocalDate.of(req.getYear(), req.getMonth(), 1);
                                                LocalDate nextMonth = currentMonth.plusMonths(1);

                                                List<CategoryMonthTotalPrice> results = categoryTotalPriceByIdRepository
                                                                .findMonthlyTotalPriceByCategoryId(
                                                                                req.getCategoryId().longValue(),
                                                                                req.getYear(),
                                                                                req.getMonth(),
                                                                                nextMonth.getYear(),
                                                                                nextMonth.getMonthValue());

                                                List<CategoriesMonthlyTotalPriceResponse> response = results.stream()
                                                                .map(CategoriesMonthlyTotalPriceResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} monthly total price stats for categoryId {}", response.size(),
                                                                req.getCategoryId());

                                                return ApiResponse.<List<CategoriesMonthlyTotalPriceResponse>>builder()
                                                                .status("success")
                                                                .message("Monthly total price by category retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch monthly total price by category | CategoryId: {}, Year: {}, Month: {}",
                                                                req.getCategoryId(), req.getYear(), req.getMonth(), e);
                                                return ApiResponse.<List<CategoriesMonthlyTotalPriceResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to fetch monthly total price by category")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<CategoriesYearlyTotalPriceResponse>> findYearlyTotalPriceById(
                        YearTotalPriceCategory req) {
                Attributes attrs = Attributes.builder()
                                .put("category.id", req.getCategoryId() != null ? req.getCategoryId().toString() : "null")
                                .put("category.year", req.getYear() != null ? req.getYear().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyTotalPriceById", "find_yearly_total_price_by_id", attrs,
                                () -> {
                                        log.info("📊 Fetching yearly total price by category | CategoryId: {}, Year: {}",
                                                        req.getCategoryId(), req.getYear());

                                        if (req.getCategoryId() == null || req.getYear() == null) {
                                                log.error("❌ CategoryId or Year is null | req: {}", req);
                                                return ApiResponse.<List<CategoriesYearlyTotalPriceResponse>>builder()
                                                                .status("error")
                                                                .message("CategoryId and Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<CategoryYearTotalPrice> results = categoryTotalPriceByIdRepository
                                                                .findYearlyTotalPriceByCategoryId(req.getCategoryId().longValue(),
                                                                                req.getYear(),
                                                                                req.getYear() - 1);

                                                List<CategoriesYearlyTotalPriceResponse> response = results.stream()
                                                                .map(CategoriesYearlyTotalPriceResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} yearly total price stats for categoryId {}", response.size(),
                                                                req.getCategoryId());

                                                return ApiResponse.<List<CategoriesYearlyTotalPriceResponse>>builder()
                                                                .status("success")
                                                                .message("Yearly total price by category retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly total price by category | CategoryId: {}, Year: {}",
                                                                req.getCategoryId(), req.getYear(), e);
                                                return ApiResponse.<List<CategoriesYearlyTotalPriceResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to fetch yearly total price by category")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }
}
