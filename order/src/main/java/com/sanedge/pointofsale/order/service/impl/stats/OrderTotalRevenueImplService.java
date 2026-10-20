package com.sanedge.pointofsale.order.service.impl.stats;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.order.domain.requests.MonthTotalRevenue;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.order.domain.responses.OrderMonthlyTotalRevenueResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderYearlyTotalRevenueResponse;
import com.sanedge.pointofsale.order.models.OrderMonthTotalRevenue;
import com.sanedge.pointofsale.order.models.OrderYearTotalRevenue;
import com.sanedge.pointofsale.order.repository.stats.OrderTotalRevenueRepository;
import com.sanedge.pointofsale.order.service.stats.OrderTotalRevenueService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderTotalRevenueImplService implements OrderTotalRevenueService {

        private final OrderTotalRevenueRepository orderTotalRevenueRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<OrderMonthlyTotalRevenueResponse>> findMonthlyStats(MonthTotalRevenue req) {
                Attributes attrs = Attributes.builder()
                                .put("year", req.getYear() != null ? req.getYear().toString() : "null")
                                .put("month", req.getMonth() != null ? req.getMonth().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyRevenueStats", "find_monthly_revenue", attrs, () -> {
                        log.info("📊 Fetching monthly order stats | Year: {}, Month: {}", req.getYear(), req.getMonth());

                        if (req.getYear() == null || req.getMonth() == null) {
                                log.error("❌ Year or Month is null | req: {}", req);
                                return ApiResponse.<List<OrderMonthlyTotalRevenueResponse>>builder()
                                                .status("error")
                                                .message("Year and Month must not be null")
                                                .data(List.of())
                                                .build();
                        }

                        try {
                                if (req.getMonth() < 1 || req.getMonth() > 12) {
                                        return ApiResponse.<List<OrderMonthlyTotalRevenueResponse>>builder()
                                                        .status("error")
                                                        .message("Month must be between 1 and 12")
                                                        .data(List.of())
                                                        .build();
                                }

                                LocalDate current = LocalDate.of(req.getYear(), req.getMonth(), 1);
                                LocalDate next = current.plusMonths(1);

                                List<OrderMonthTotalRevenue> rawData = orderTotalRevenueRepository.findMonthlyTotalRevenue(
                                                req.getYear(),
                                                req.getMonth(),
                                                next.getYear(), next.getMonthValue());

                                List<OrderMonthlyTotalRevenueResponse> response = rawData.stream()
                                                .map(OrderMonthlyTotalRevenueResponse::from)
                                                .collect(Collectors.toList());

                                log.info("✅ Found {} monthly order stats", response.size());

                                return ApiResponse.<List<OrderMonthlyTotalRevenueResponse>>builder()
                                                .status("success")
                                                .message("Monthly order stats retrieved successfully")
                                                .data(response)
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch monthly order stats | Year: {}, Month: {}",
                                                req.getYear(), req.getMonth(), e);
                                return ApiResponse.<List<OrderMonthlyTotalRevenueResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch monthly order stats. Please try again later.")
                                                .data(List.of())
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<List<OrderYearlyTotalRevenueResponse>> findYearlyStats(Integer year) {
                Attributes attrs = Attributes.builder()
                                .put("year", year != null ? year.toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyRevenueStats", "find_yearly_revenue", attrs, () -> {
                        log.info("📈 Fetching yearly order stats | Year: {}", year);

                        if (year == null) {
                                log.error("❌ Year is null | req: {}", year);
                                return ApiResponse.<List<OrderYearlyTotalRevenueResponse>>builder()
                                                .status("error")
                                                .message("Year must not be null")
                                                .data(List.of())
                                                .build();
                        }

                        try {
                                List<OrderYearTotalRevenue> rawData = orderTotalRevenueRepository.findYearlyTotalRevenue(year);

                                List<OrderYearlyTotalRevenueResponse> response = rawData.stream()
                                                .map(OrderYearlyTotalRevenueResponse::from)
                                                .collect(Collectors.toList());

                                log.info("✅ Found {} yearly order stats", response.size());

                                return ApiResponse.<List<OrderYearlyTotalRevenueResponse>>builder()
                                                .status("success")
                                                .message("Yearly order stats retrieved successfully")
                                                .data(response)
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch yearly order stats | Year: {}", year, e);
                                return ApiResponse.<List<OrderYearlyTotalRevenueResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch yearly order stats. Please try again later.")
                                                .data(List.of())
                                                .build();
                        }
                });
        }
}
