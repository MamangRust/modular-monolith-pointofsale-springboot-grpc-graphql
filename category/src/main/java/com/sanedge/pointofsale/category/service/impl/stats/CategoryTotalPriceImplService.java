package com.sanedge.pointofsale.category.service.impl.stats;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.category.domain.requests.MonthTotalPrice;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.category.domain.responses.CategoriesMonthlyTotalPriceResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesYearlyTotalPriceResponse;
import com.sanedge.pointofsale.category.models.CategoryMonthTotalPrice;
import com.sanedge.pointofsale.category.models.CategoryYearTotalPrice;
import com.sanedge.pointofsale.category.repository.stats.CategoryTotalPriceRepository;
import com.sanedge.pointofsale.category.service.stats.CategoryTotalPriceService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryTotalPriceImplService implements CategoryTotalPriceService {
        private final CategoryTotalPriceRepository categoryTotalPriceRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<CategoriesMonthlyTotalPriceResponse>> findMonthlyTotalPrice(
                        MonthTotalPrice req) {
                Attributes attrs = Attributes.builder()
                                .put("category.year", req.getYear() != null ? req.getYear().toString() : "null")
                                .put("category.month", req.getMonth() != null ? req.getMonth().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyTotalPrice", "find_monthly_total_price", attrs, () -> {
                        log.info("📊 Fetching monthly total category price | Year: {}, Month: {}",
                                        req.getYear(), req.getMonth());

                        if (req.getYear() == null || req.getMonth() == null) {
                                log.error("❌ Year or Month is null | req: {}", req);
                                return ApiResponse.<List<CategoriesMonthlyTotalPriceResponse>>builder()
                                                .status("error")
                                                .message("Year and Month must not be null")
                                                .data(List.of())
                                                .build();
                        }

                        try {
                                LocalDate currentMonth = LocalDate.of(req.getYear(), req.getMonth(), 1);
                                LocalDate nextMonth = currentMonth.plusMonths(1);

                                List<CategoryMonthTotalPrice> results = categoryTotalPriceRepository.findMonthlyTotalPrice(
                                                req.getYear(),
                                                req.getMonth(),
                                                nextMonth.getYear(),
                                                nextMonth.getMonthValue());

                                List<CategoriesMonthlyTotalPriceResponse> response = results.stream()
                                                .map(CategoriesMonthlyTotalPriceResponse::from)
                                                .toList();

                                log.info("✅ Found {} monthly total price stats", response.size());

                                return ApiResponse.<List<CategoriesMonthlyTotalPriceResponse>>builder()
                                                .status("success")
                                                .message("Monthly total price stats retrieved successfully")
                                                .data(response)
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch monthly total category price | Year: {}, Month: {}",
                                                req.getYear(), req.getMonth(), e);
                                return ApiResponse.<List<CategoriesMonthlyTotalPriceResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch monthly total category price")
                                                .data(List.of())
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<List<CategoriesYearlyTotalPriceResponse>> findYearlyTotalPrice(Integer year) {
                Attributes attrs = Attributes.builder()
                                .put("category.year", year != null ? year.toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyTotalPrice", "find_yearly_total_price", attrs, () -> {
                        log.info("📊 Fetching yearly total category price | Year: {}", year);
                        try {
                                List<CategoryYearTotalPrice> results = categoryTotalPriceRepository
                                                .findYearlyTotalPrice(year, year - 1);

                                List<CategoriesYearlyTotalPriceResponse> response = results.stream()
                                                .map(CategoriesYearlyTotalPriceResponse::from)
                                                .toList();

                                log.info("✅ Found {} yearly total price stats", response.size());

                                return ApiResponse.<List<CategoriesYearlyTotalPriceResponse>>builder()
                                                .status("success")
                                                .message("Yearly total price stats retrieved successfully")
                                                .data(response)
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch yearly total category price | Year: {}", year, e);
                                return ApiResponse.<List<CategoriesYearlyTotalPriceResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch yearly total category price")
                                                .data(List.of())
                                                .build();
                        }
                });
        }
}
