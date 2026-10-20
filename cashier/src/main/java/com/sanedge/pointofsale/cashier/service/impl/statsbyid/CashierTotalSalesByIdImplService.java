package com.sanedge.pointofsale.cashier.service.impl.statsbyid;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.cashier.domain.requests.MonthTotalSalesCashier;
import com.sanedge.pointofsale.cashier.domain.requests.YearTotalSalesCashier;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthTotalSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearTotalSales;
import com.sanedge.pointofsale.cashier.models.CashierMonthTotalSales;
import com.sanedge.pointofsale.cashier.models.CashierYearTotalSales;
import com.sanedge.pointofsale.cashier.repository.statsbyid.CashierTotalSalesByIdRepository;
import com.sanedge.pointofsale.cashier.service.statsbyid.CashierTotalSalesByIdService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CashierTotalSalesByIdImplService implements CashierTotalSalesByIdService {
        private final CashierTotalSalesByIdRepository cashierTotalSalesByIdRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<CashierResponseMonthTotalSales>> findMonthlyTotalSalesById(MonthTotalSalesCashier req) {
                Attributes attrs = Attributes.builder()
                                .put("cashier.id", req.getCashierId() != null ? req.getCashierId().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyTotalSalesById", "find_monthly_total_sales_by_id", attrs,
                                () -> {
                                        log.info("📊 Fetching monthly total cashier sales by cashierId | CashierID: {}, Year: {}, Month: {}",
                                                        req.getCashierId(), req.getYear(), req.getMonth());

                                        if (req.getCashierId() == null || req.getYear() == null || req.getMonth() == null) {
                                                log.error("❌ CashierId, Year, or Month is null | req: {}", req);
                                                return ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                                                                .status("error")
                                                                .message("CashierId, Year, and Month must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                LocalDate currentMonth = LocalDate.of(req.getYear(), req.getMonth(), 1);
                                                LocalDate nextMonth = currentMonth.plusMonths(1);

                                                List<CashierMonthTotalSales> results = cashierTotalSalesByIdRepository.findMonthTotalSalesById(
                                                                req.getCashierId().longValue(),
                                                                req.getYear(),
                                                                req.getMonth(),
                                                                nextMonth.getYear(),
                                                                nextMonth.getMonthValue());

                                                List<CashierResponseMonthTotalSales> response = results.stream()
                                                                .map(CashierResponseMonthTotalSales::from)
                                                                .toList();

                                                log.info("✅ Found {} monthly total cashier sales for cashier {}", response.size(),
                                                                req.getCashierId());

                                                return ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                                                                .status("success")
                                                                .message("Monthly total cashier sales by cashier retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error(
                                                                "💥 Failed to fetch monthly total cashier sales by cashier | CashierID: {}, Year: {}, Month: {}",
                                                                req.getCashierId(), req.getYear(), req.getMonth(), e);
                                                return ApiResponse.<List<CashierResponseMonthTotalSales>>builder()
                                                                .status("error")
                                                                .message("Failed to fetch monthly total cashier sales by cashier")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<CashierResponseYearTotalSales>> findYearlyTotalSalesById(YearTotalSalesCashier req) {
                Attributes attrs = Attributes.builder()
                                .put("cashier.id", req.getCashierId() != null ? req.getCashierId().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyTotalSalesById", "find_yearly_total_sales_by_id", attrs,
                                () -> {
                                        log.info("📊 Fetching yearly total cashier sales by cashierId | CashierID: {}, Year: {}",
                                                        req.getCashierId(), req.getYear());

                                        if (req.getCashierId() == null || req.getYear() == null) {
                                                log.error("❌ CashierId or Year is null | req: {}", req);
                                                return ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                                                                .status("error")
                                                                .message("CashierId and Year must not be null")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<CashierYearTotalSales> results = cashierTotalSalesByIdRepository.findYearTotalSalesById(
                                                                req.getCashierId().longValue(),
                                                                req.getYear(),
                                                                req.getYear() - 1);

                                                List<CashierResponseYearTotalSales> response = results.stream()
                                                                .map(CashierResponseYearTotalSales::from)
                                                                .toList();

                                                log.info("✅ Found {} yearly total cashier sales for cashier {}", response.size(),
                                                                req.getCashierId());

                                                return ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                                                                .status("success")
                                                                .message("Yearly total cashier sales by cashier retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly total cashier sales by cashier | CashierID: {}, Year: {}",
                                                                req.getCashierId(), req.getYear(), e);
                                                return ApiResponse.<List<CashierResponseYearTotalSales>>builder()
                                                                .status("error")
                                                                .message("Failed to fetch yearly total cashier sales by cashier")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }
}
