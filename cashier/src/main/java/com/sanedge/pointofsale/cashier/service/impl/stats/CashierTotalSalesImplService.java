package com.sanedge.pointofsale.cashier.service.impl.stats;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.cashier.domain.requests.MonthTotalSales;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthTotalSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearTotalSales;
import com.sanedge.pointofsale.cashier.models.CashierMonthTotalSales;
import com.sanedge.pointofsale.cashier.models.CashierYearTotalSales;
import com.sanedge.pointofsale.cashier.repository.stats.CashierTotalSalesRepository;
import com.sanedge.pointofsale.cashier.service.stats.CashierTotalSalesService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CashierTotalSalesImplService implements CashierTotalSalesService {
        private final CashierTotalSalesRepository cashierTotalSalesRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<CashierResponseMonthTotalSales>> findMonthlyTotalSales(MonthTotalSales req) {
                Attributes attrs = Attributes.builder()
                                .put("year", req.getYear() != null ? req.getYear().toString() : "null")
                                .put("month", req.getMonth() != null ? req.getMonth().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyTotalSales", "find_monthly_total_sales", attrs, () -> {
                        log.info("📊 Fetching monthly total cashier sales | Year: {}, Month: {}", req.getYear(),
                                        req.getMonth());

                        if (req.getYear() == null || req.getMonth() == null) {
                                log.error("❌ Year or Month is null | req: {}", req);
                                return ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                                                .status("error")
                                                .message("Year and Month must not be null")
                                                .data(List.of())
                                                .build();
                        }

                        try {
                                LocalDate currentMonth = LocalDate.of(req.getYear(), req.getMonth(), 1);
                                LocalDate nextMonth = currentMonth.plusMonths(1);

                                List<CashierMonthTotalSales> results = cashierTotalSalesRepository.findMonthTotalSales(
                                                req.getYear(),
                                                req.getMonth(),
                                                nextMonth.getYear(),
                                                nextMonth.getMonthValue());

                                List<CashierResponseMonthTotalSales> response = results.stream()
                                                .map(CashierResponseMonthTotalSales::from)
                                                .toList();

                                log.info("✅ Found {} monthly total cashier sales", response.size());

                                return ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                                                .status("success")
                                                .message("Monthly total cashier sales retrieved successfully")
                                                .data(response)
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch monthly total cashier sales | Year: {}, Month: {}",
                                                req.getYear(), req.getMonth(), e);
                                return ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                                                .status("error")
                                                .message("Failed to fetch monthly total cashier sales")
                                                .data(List.of())
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<List<CashierResponseYearTotalSales>> findYearlyTotalSales(Integer year) {
                Attributes attrs = Attributes.builder()
                                .put("year", year != null ? year.toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyTotalSales", "find_yearly_total_sales", attrs, () -> {
                        log.info("📊 Fetching yearly total cashier sales | Year: {}", year);

                        if (year == null) {
                                log.error("❌ Year is null");
                                return ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                                                .status("error")
                                                .message("Year must not be null")
                                                .data(List.of())
                                                .build();
                        }

                        try {
                                List<CashierYearTotalSales> results = cashierTotalSalesRepository.findYearTotalSales(
                                                year,
                                                year - 1);

                                List<CashierResponseYearTotalSales> response = results.stream()
                                                .map(CashierResponseYearTotalSales::from)
                                                .toList();

                                log.info("✅ Found {} yearly total cashier sales", response.size());

                                return ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                                                .status("success")
                                                .message("Yearly total cashier sales retrieved successfully")
                                                .data(response)
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch yearly total cashier sales | Year: {}", year, e);
                                return ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                                                .status("error")
                                                .message("Failed to fetch yearly total cashier sales")
                                                .data(List.of())
                                                .build();
                        }
                });
        }
}
