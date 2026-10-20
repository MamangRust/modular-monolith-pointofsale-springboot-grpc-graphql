package com.sanedge.pointofsale.cashier.service.impl.stats;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales;
import com.sanedge.pointofsale.cashier.models.CashierMonthSales;
import com.sanedge.pointofsale.cashier.models.CashierYearSales;
import com.sanedge.pointofsale.cashier.repository.stats.CashierSalesRepository;
import com.sanedge.pointofsale.cashier.service.stats.CashierSalesService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CashierSalesImplService implements CashierSalesService {
    private final CashierSalesRepository cashierSalesRepository;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<List<CashierResponseMonthSales>> findMonthlySales(Integer year) {
        Attributes attrs = Attributes.builder()
                .put("year", year != null ? year.toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("findMonthlySales", "find_monthly_sales", attrs, () -> {
            log.info("📊 Fetching monthly cashier sales | Year: {}", year);
            try {
                List<CashierMonthSales> results = cashierSalesRepository.findMonthSales(year, 1, 12);

                List<CashierResponseMonthSales> response = results.stream()
                        .map(CashierResponseMonthSales::from)
                        .toList();

                log.info("✅ Found {} monthly cashier sales records", response.size());

                return ApiResponse.<List<CashierResponseMonthSales>>builder()
                        .status("success")
                        .message("Monthly cashier sales retrieved successfully")
                        .data(response)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to fetch monthly cashier sales | Year: {}", year, e);
                return ApiResponse.<List<CashierResponseMonthSales>>builder()
                        .status("error")
                        .message("Failed to fetch monthly cashier sales")
                        .data(List.of())
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<List<CashierResponseYearSales>> findYearlySales(Integer year) {
        Attributes attrs = Attributes.builder()
                .put("year", year != null ? year.toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("findYearlySales", "find_yearly_sales", attrs, () -> {
            log.info("📊 Fetching yearly cashier sales | Year: {}", year);
            try {
                List<CashierYearSales> results = cashierSalesRepository.findYearSales(year);

                List<CashierResponseYearSales> response = results.stream()
                        .map(CashierResponseYearSales::from)
                        .toList();

                log.info("✅ Found {} yearly cashier sales records", response.size());

                return ApiResponse.<List<CashierResponseYearSales>>builder()
                        .status("success")
                        .message("Yearly cashier sales retrieved successfully")
                        .data(response)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to fetch yearly cashier sales | Year: {}", year, e);
                return ApiResponse.<List<CashierResponseYearSales>>builder()
                        .status("error")
                        .message("Failed to fetch yearly cashier sales")
                        .data(List.of())
                        .build();
            }
        });
    }
}
