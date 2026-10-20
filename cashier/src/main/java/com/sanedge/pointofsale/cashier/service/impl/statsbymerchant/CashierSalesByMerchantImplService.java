package com.sanedge.pointofsale.cashier.service.impl.statsbymerchant;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.cashier.domain.requests.MonthCashierMerchantRequest;
import com.sanedge.pointofsale.cashier.domain.requests.YearCashierMerchantRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales;
import com.sanedge.pointofsale.cashier.models.CashierMonthSales;
import com.sanedge.pointofsale.cashier.models.CashierYearSales;
import com.sanedge.pointofsale.cashier.repository.statsbymerchant.CashierSalesByMerchantRepository;
import com.sanedge.pointofsale.cashier.service.statsbymerchant.CashierSalesByMerchantService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CashierSalesByMerchantImplService implements CashierSalesByMerchantService {
        private final CashierSalesByMerchantRepository cashierSalesByMerchantRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<CashierResponseMonthSales>> findMonthlyCashierByMerchant(
                        MonthCashierMerchantRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("merchant.id", req.getMerchantId() != null ? req.getMerchantId().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyCashierByMerchant", "find_monthly_cashier_by_merchant",
                                attrs, () -> {
                                        log.info("📊 Fetching monthly cashier sales by merchant | MerchantID: {}, Year: {}",
                                                        req.getMerchantId(), req.getYear());

                                        if (req.getMerchantId() == null || req.getYear() == null) {
                                                log.error("❌ MerchantId or Year is null | req: {}", req);
                                                return ApiResponse.<List<CashierResponseMonthSales>>builder()
                                                                .status("error")
                                                                .message("MerchantId and Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                LocalDate startMonth = LocalDate.of(req.getYear(), 1, 1);
                                                LocalDate nextYear = startMonth.plusYears(1);

                                                List<CashierMonthSales> results = cashierSalesByMerchantRepository.findMonthSalesByMerchant(
                                                                req.getMerchantId().longValue(),
                                                                req.getYear(),
                                                                startMonth.getMonthValue(),
                                                                nextYear.getMonthValue());

                                                List<CashierResponseMonthSales> response = results.stream()
                                                                .map(CashierResponseMonthSales::from)
                                                                .toList();

                                                log.info("✅ Found {} monthly cashier sales for merchant {}", response.size(),
                                                                req.getMerchantId());

                                                return ApiResponse.<List<CashierResponseMonthSales>>builder()
                                                                .status("success")
                                                                .message("Monthly cashier sales by merchant retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch monthly cashier sales by merchant | MerchantID: {}, Year: {}",
                                                                req.getMerchantId(), req.getYear(), e);
                                                return ApiResponse.<List<CashierResponseMonthSales>>builder()
                                                                .status("error")
                                                                .message("Failed to fetch monthly cashier sales by merchant")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<CashierResponseYearSales>> findYearlyCashierByMerchant(YearCashierMerchantRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("merchant.id", req.getMerchantId() != null ? req.getMerchantId().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyCashierByMerchant", "find_yearly_cashier_by_merchant",
                                attrs, () -> {
                                        log.info("📊 Fetching yearly cashier sales by merchant | MerchantID: {}, Year: {}",
                                                        req.getMerchantId(), req.getYear());

                                        if (req.getMerchantId() == null || req.getYear() == null) {
                                                log.error("❌ MerchantId or Year is null | req: {}", req);
                                                return ApiResponse.<List<CashierResponseYearSales>>builder()
                                                                .status("error")
                                                                .message("MerchantId and Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<CashierYearSales> results = cashierSalesByMerchantRepository.findYearSalesByMerchant(
                                                                req.getMerchantId().longValue(),
                                                                req.getYear());

                                                List<CashierResponseYearSales> response = results.stream()
                                                                .map(CashierResponseYearSales::from)
                                                                .toList();

                                                log.info("✅ Found {} yearly cashier sales for merchant {}", response.size(),
                                                                req.getMerchantId());

                                                return ApiResponse.<List<CashierResponseYearSales>>builder()
                                                                .status("success")
                                                                .message("Yearly cashier sales by merchant retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly cashier sales by merchant | MerchantID: {}, Year: {}",
                                                                req.getMerchantId(), req.getYear(), e);
                                                return ApiResponse.<List<CashierResponseYearSales>>builder()
                                                                .status("error")
                                                                .message("Failed to fetch yearly cashier sales by merchant")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }
}
