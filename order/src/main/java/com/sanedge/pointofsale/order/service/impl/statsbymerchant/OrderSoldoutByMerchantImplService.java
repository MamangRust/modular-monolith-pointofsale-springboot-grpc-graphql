package com.sanedge.pointofsale.order.service.impl.statsbymerchant;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.order.domain.requests.MonthOrderMerchantRequest;
import com.sanedge.pointofsale.order.domain.requests.YearOrderMerchantRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.order.domain.responses.OrderMonthlyResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderYearlyResponse;
import com.sanedge.pointofsale.order.models.OrderMonth;
import com.sanedge.pointofsale.order.models.OrderYear;
import com.sanedge.pointofsale.order.repository.statsbymerchant.OrderSoldOutByMerchantRepository;
import com.sanedge.pointofsale.order.service.statsbymerchant.OrderSoldOutByMerchantService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderSoldoutByMerchantImplService implements OrderSoldOutByMerchantService {

        private final OrderSoldOutByMerchantRepository orderSoldOutByMerchantRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponse<List<OrderMonthlyResponse>> findMonthlyOrdersByMerchant(MonthOrderMerchantRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("merchant.id", req.getMerchantId() != null ? req.getMerchantId().toString() : "null")
                                .put("yearMonth", req.getYear() != null ? req.getYear().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findMonthlyOrdersByMerchant", "find_merchant_monthly_orders",
                                attrs, () -> {
                                        log.info("📊 Fetching monthly orders for merchant | merchantId={}, year={}", req.getMerchantId(),
                                                        req.getYear());

                                        if (req.getMerchantId() == null || req.getYear() == null) {
                                                log.error("❌ Missing required fields | req: {}", req);
                                                return ApiResponse.<List<OrderMonthlyResponse>>builder()
                                                                .status("error")
                                                                .message("Merchant ID and Year are required")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<OrderMonth> rawData = orderSoldOutByMerchantRepository
                                                                .findMonthlyOrdersByMerchant(req.getMerchantId(), req.getYear());

                                                List<OrderMonthlyResponse> response = rawData.stream()
                                                                .map(OrderMonthlyResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} monthly order records for merchant", response.size());

                                                return ApiResponse.<List<OrderMonthlyResponse>>builder()
                                                                .status("success")
                                                                .message("Monthly order stats by merchant retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch monthly orders for merchant | merchantId={}, year={}",
                                                                req.getMerchantId(), req.getYear(), e);
                                                return ApiResponse.<List<OrderMonthlyResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve monthly order data. Please try again later.")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<List<OrderYearlyResponse>> findYearlyOrdersByMerchant(YearOrderMerchantRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("merchant.id", req.getMerchantId() != null ? req.getMerchantId().toString() : "null")
                                .put("year", req.getYear() != null ? req.getYear().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findYearlyOrdersByMerchant", "find_merchant_yearly_orders",
                                attrs, () -> {
                                        log.info("📈 Fetching yearly orders for merchant | merchantId={}, year={}", req.getMerchantId(),
                                                        req.getYear());

                                        if (req.getMerchantId() == null || req.getYear() == null) {
                                                log.error("❌ Missing required fields | req: {}", req);
                                                return ApiResponse.<List<OrderYearlyResponse>>builder()
                                                                .status("error")
                                                                .message("Merchant ID and Year are required")
                                                                .data(List.of())
                                                                .build();
                                        }

                                        try {
                                                List<OrderYear> rawData = orderSoldOutByMerchantRepository
                                                                .findYearlyOrdersByMerchant(req.getMerchantId(), req.getYear());

                                                List<OrderYearlyResponse> response = rawData.stream()
                                                                .map(OrderYearlyResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} yearly order records for merchant", response.size());

                                                return ApiResponse.<List<OrderYearlyResponse>>builder()
                                                                .status("success")
                                                                .message("Yearly order stats by merchant retrieved successfully")
                                                                .data(response)
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch yearly orders for merchant | merchantId={}, year={}",
                                                                req.getMerchantId(), req.getYear(), e);
                                                return ApiResponse.<List<OrderYearlyResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to retrieve yearly order data. Please try again later.")
                                                                .data(List.of())
                                                                .build();
                                        }
                                });
        }
}
