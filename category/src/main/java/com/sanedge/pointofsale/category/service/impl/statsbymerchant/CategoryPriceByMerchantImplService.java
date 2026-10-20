package com.sanedge.pointofsale.category.service.impl.statsbymerchant;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.category.domain.requests.MonthPriceMerchant;
import com.sanedge.pointofsale.category.domain.requests.YearPriceMerchant;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.category.domain.responses.CategoriesMonthPriceResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoriesYearPriceResponse;
import com.sanedge.pointofsale.category.models.CategoryMonthPrice;
import com.sanedge.pointofsale.category.models.CategoryYearPrice;
import com.sanedge.pointofsale.category.repository.statsbymerchant.CategoryPriceByMerchantRepository;
import com.sanedge.pointofsale.category.service.statsbymerchant.CategoryPriceByMerchantService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryPriceByMerchantImplService implements CategoryPriceByMerchantService {

        private final CategoryPriceByMerchantRepository categoryPriceByMerchantRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<CategoriesMonthPriceResponse>> findMonthPriceByMerchant(MonthPriceMerchant req) {
                Attributes attrs = Attributes.builder()
                                .put("merchant.id", req.getMerchantId() != null ? req.getMerchantId().toString() : "null")
                                .put("category.year", req.getYear() != null ? req.getYear().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthPriceByMerchant", "find_month_price_by_merchant", attrs,
                                () -> {
                                        log.info("📊 Fetching monthly price by merchant | MerchantId: {}, Year: {}", req.getMerchantId(),
                                                        req.getYear());

                                        if (req.getMerchantId() == null || req.getYear() == null) {
                                                return ApiResponse.<List<CategoriesMonthPriceResponse>>builder()
                                                                .status("error")
                                                                .message("MerchantId and Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<CategoryMonthPrice> results = categoryPriceByMerchantRepository
                                                                .findMonthlyCategoryPriceByMerchant(req.getMerchantId().longValue(),
                                                                                req.getYear());

                                                List<CategoriesMonthPriceResponse> response = results.stream()
                                                                .map(CategoriesMonthPriceResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} monthly price stats for merchant {}", response.size(),
                                                                req.getMerchantId());

                                                return ApiResponse.<List<CategoriesMonthPriceResponse>>builder()
                                                                .status("success")
                                                                .message("Monthly price by merchant retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Error fetching monthly price by merchant | MerchantId: {}, Year: {}",
                                                                req.getMerchantId(), req.getYear(), e);

                                                return ApiResponse.<List<CategoriesMonthPriceResponse>>builder()
                                                                .status("error")
                                                                .message("Unable to fetch monthly price data at the moment")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<CategoriesYearPriceResponse>> findYearPriceByMerchant(YearPriceMerchant req) {
                Attributes attrs = Attributes.builder()
                                .put("merchant.id", req.getMerchantId() != null ? req.getMerchantId().toString() : "null")
                                .put("category.year", req.getYear() != null ? req.getYear().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findYearPriceByMerchant", "find_year_price_by_merchant", attrs,
                                () -> {
                                        log.info("📊 Fetching yearly price by merchant | MerchantId: {}, Year: {}", req.getMerchantId(),
                                                        req.getYear());

                                        if (req.getMerchantId() == null || req.getYear() == null) {
                                                return ApiResponse.<List<CategoriesYearPriceResponse>>builder()
                                                                .status("error")
                                                                .message("MerchantId and Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<CategoryYearPrice> results = categoryPriceByMerchantRepository
                                                                .findYearlyCategoryPriceByMerchant(req.getMerchantId().longValue(),
                                                                                req.getYear());

                                                List<CategoriesYearPriceResponse> response = results.stream()
                                                                .map(CategoriesYearPriceResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} yearly price stats for merchant {}", response.size(), req.getMerchantId());

                                                return ApiResponse.<List<CategoriesYearPriceResponse>>builder()
                                                                .status("success")
                                                                .message("Yearly price by merchant retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Error fetching yearly price by merchant | MerchantId: {}, Year: {}",
                                                                req.getMerchantId(), req.getYear(), e);

                                                return ApiResponse.<List<CategoriesYearPriceResponse>>builder()
                                                                .status("error")
                                                                .message("Unable to fetch yearly price data at the moment")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }
}
