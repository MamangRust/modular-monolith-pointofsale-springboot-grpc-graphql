package com.sanedge.pointofsale.category.service.impl.stats;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.category.domain.responses.CategoriesMonthPriceResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesYearPriceResponse;
import com.sanedge.pointofsale.category.models.CategoryMonthPrice;
import com.sanedge.pointofsale.category.models.CategoryYearPrice;
import com.sanedge.pointofsale.category.repository.stats.CategoryPriceRepository;
import com.sanedge.pointofsale.category.service.stats.CategoryPriceService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryPriceImplService implements CategoryPriceService {
    private final CategoryPriceRepository categoryPriceRepository;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<List<CategoriesMonthPriceResponse>> findMonthPrice(Integer year) {
        Attributes attrs = Attributes.builder()
                .put("category.year", year != null ? year.toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("findMonthPrice", "find_month_price", attrs, () -> {
            log.info("📊 Fetching monthly category price stats | Year: {}", year);
            try {
                List<CategoryMonthPrice> results = categoryPriceRepository.findMonthlyCategoryPrice(year);

                List<CategoriesMonthPriceResponse> response = results.stream()
                        .map(CategoriesMonthPriceResponse::from)
                        .toList();

                log.info("✅ Found {} monthly category price stats", response.size());

                return ApiResponse.<List<CategoriesMonthPriceResponse>>builder()
                        .status("success")
                        .message("Monthly category price stats retrieved successfully")
                        .data(response)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to fetch monthly category price stats | Year: {}", year, e);
                return ApiResponse.<List<CategoriesMonthPriceResponse>>builder()
                        .status("error")
                        .message("Failed to fetch monthly category price stats")
                        .data(List.of())
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<List<CategoriesYearPriceResponse>> findYearPrice(Integer year) {
        Attributes attrs = Attributes.builder()
                .put("category.year", year != null ? year.toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("findYearPrice", "find_year_price", attrs, () -> {
            log.info("📊 Fetching yearly category price stats | Year: {}", year);
            try {
                List<CategoryYearPrice> results = categoryPriceRepository.findYearlyCategoryPrice(year);

                List<CategoriesYearPriceResponse> response = results.stream()
                        .map(CategoriesYearPriceResponse::from)
                        .toList();

                log.info("✅ Found {} yearly category price stats", response.size());

                return ApiResponse.<List<CategoriesYearPriceResponse>>builder()
                        .status("success")
                        .message("Yearly category price stats retrieved successfully")
                        .data(response)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to fetch yearly category price stats | Year: {}", year, e);
                return ApiResponse.<List<CategoriesYearPriceResponse>>builder()
                        .status("error")
                        .message("Failed to fetch yearly category price stats")
                        .data(List.of())
                        .build();
            }
        });
    }
}
