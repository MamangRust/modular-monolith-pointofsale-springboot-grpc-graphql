package com.sanedge.pointofsale.cashier.service.impl.statsbyid;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.cashier.domain.requests.MonthCashierIdRequest;
import com.sanedge.pointofsale.cashier.domain.requests.YearCashierIdRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales;
import com.sanedge.pointofsale.cashier.models.CashierMonthSales;
import com.sanedge.pointofsale.cashier.models.CashierYearSales;
import com.sanedge.pointofsale.cashier.repository.statsbyid.CashierSalesByIdRepository;
import com.sanedge.pointofsale.cashier.service.statsbyid.CashierSalesByIdService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CashierSalesByIdImplService implements CashierSalesByIdService {
        private final CashierSalesByIdRepository cashierSalesByIdRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<CashierResponseMonthSales>> findMonthlyCashierById(MonthCashierIdRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("cashier.id", req.getCashierId() != null ? req.getCashierId().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyCashierById", "find_monthly_cashier_by_id", attrs,
                                () -> {
                                        log.info("📊 Fetching monthly cashier sales by cashierId | CashierID: {}, Year: {}",
                                                        req.getCashierId(), req.getYear());

                                        if (req.getCashierId() == null || req.getYear() == null) {
                                                log.error("❌ CashierId or Year is null | req: {}", req);
                                                return ApiResponse.<List<CashierResponseMonthSales>>builder()
                                                                .status("error")
                                                                .message("CashierId and Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                LocalDate startMonth = LocalDate.of(req.getYear(), 1, 1);
                                                LocalDate nextYear = startMonth.plusYears(1);

                                                List<CashierMonthSales> results = cashierSalesByIdRepository.findMonthSalesById(
                                                                req.getCashierId().longValue(),
                                                                req.getYear(),
                                                                startMonth.getMonthValue(),
                                                                nextYear.getMonthValue());

                                                List<CashierResponseMonthSales> response = results.stream()
                                                                .map(CashierResponseMonthSales::from)
                                                                .toList();

                                                log.info("✅ Found {} monthly cashier sales for cashier {}", response.size(),
                                                                req.getCashierId());

                                                return ApiResponse.<List<CashierResponseMonthSales>>builder()
                                                                .status("success")
                                                                .message("Monthly cashier sales by cashier retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch monthly cashier sales by cashier | CashierID: {}, Year: {}",
                                                                req.getCashierId(), req.getYear(), e);
                                                return ApiResponse.<List<CashierResponseMonthSales>>builder()
                                                                .status("error")
                                                                .message("Failed to fetch monthly cashier sales by cashier")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<CashierResponseYearSales>> findYearlyCashierById(YearCashierIdRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("cashier.id", req.getCashierId() != null ? req.getCashierId().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyCashierById", "find_yearly_cashier_by_id", attrs,
                                () -> {
                                        log.info("📊 Fetching yearly cashier sales by cashierId | CashierID: {}, Year: {}",
                                                        req.getCashierId(), req.getYear());

                                        if (req.getCashierId() == null || req.getYear() == null) {
                                                log.error("❌ CashierId or Year is null | req: {}", req);
                                                return ApiResponse.<List<CashierResponseYearSales>>builder()
                                                                .status("error")
                                                                .message("CashierId and Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<CashierYearSales> results = cashierSalesByIdRepository.findYearSalesById(
                                                                req.getCashierId().longValue(),
                                                                req.getYear());

                                                List<CashierResponseYearSales> response = results.stream()
                                                                .map(CashierResponseYearSales::from)
                                                                .toList();

                                                log.info("✅ Found {} yearly cashier sales for cashier {}", response.size(), req.getCashierId());

                                                return ApiResponse.<List<CashierResponseYearSales>>builder()
                                                                .status("success")
                                                                .message("Yearly cashier sales by cashier retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly cashier sales by cashier | CashierID: {}, Year: {}",
                                                                req.getCashierId(), req.getYear(), e);
                                                return ApiResponse.<List<CashierResponseYearSales>>builder()
                                                                .status("error")
                                                                .message("Failed to fetch yearly cashier sales by cashier")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }
}
