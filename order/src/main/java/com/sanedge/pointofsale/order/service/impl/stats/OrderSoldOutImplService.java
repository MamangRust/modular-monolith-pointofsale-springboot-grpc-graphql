package com.sanedge.pointofsale.order.service.impl.stats;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.order.domain.responses.OrderMonthlyResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderYearlyResponse;
import com.sanedge.pointofsale.order.models.OrderMonth;
import com.sanedge.pointofsale.order.models.OrderYear;
import com.sanedge.pointofsale.order.repository.stats.OrderSoldOutRepository;
import com.sanedge.pointofsale.order.service.stats.OrderSoldoutService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderSoldOutImplService implements OrderSoldoutService {

    private final OrderSoldOutRepository orderSoldOutRepository;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<List<OrderMonthlyResponse>> findMonthlyOrders(Integer yearMonth) {
        Attributes attrs = Attributes.builder()
                .put("yearMonth", yearMonth != null ? yearMonth.toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("findMonthlyOrders", "find_monthly_orders", attrs, () -> {
            log.info("📊 Fetching monthly orders for yearMonth={}", yearMonth);
            try {
                List<OrderMonth> rawData = orderSoldOutRepository.findMonthlyOrdersByYear(yearMonth);
                List<OrderMonthlyResponse> response = rawData.stream()
                        .map(OrderMonthlyResponse::from)
                        .toList();

                return ApiResponse.<List<OrderMonthlyResponse>>builder()
                        .status("success")
                        .message("Monthly order data retrieved successfully")
                        .data(response)
                        .build();

            } catch (Exception e) {
                log.error("💥 Failed to fetch monthly orders for yearMonth={}", yearMonth, e);
                return ApiResponse.<List<OrderMonthlyResponse>>builder()
                        .status("error")
                        .message("Failed to retrieve monthly order data. Please try again later.")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<List<OrderYearlyResponse>> findYearlyOrders(Integer yearMonth) {
        Attributes attrs = Attributes.builder()
                .put("yearMonth", yearMonth != null ? yearMonth.toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("findYearlyOrders", "find_yearly_orders", attrs, () -> {
            log.info("📈 Fetching yearly orders for yearMonth={}", yearMonth);
            try {
                List<OrderYear> rawData = orderSoldOutRepository.findYearlyOrders(yearMonth);
                List<OrderYearlyResponse> response = rawData.stream()
                        .map(OrderYearlyResponse::from)
                        .toList();

                return ApiResponse.<List<OrderYearlyResponse>>builder()
                        .status("success")
                        .message("Yearly order data retrieved successfully")
                        .data(response)
                        .build();

            } catch (Exception e) {
                log.error("💥 Failed to fetch yearly orders for yearMonth={}", yearMonth, e);
                return ApiResponse.<List<OrderYearlyResponse>>builder()
                        .status("error")
                        .message("Failed to retrieve yearly order data. Please try again later.")
                        .data(null)
                        .build();
            }
        });
    }
}
