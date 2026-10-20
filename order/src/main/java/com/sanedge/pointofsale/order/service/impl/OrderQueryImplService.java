package com.sanedge.pointofsale.order.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.order.domain.requests.FindAllOrderByMerchantRequest;
import com.sanedge.pointofsale.order.domain.requests.FindAllOrderRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.domain.responses.api.PaginationMeta;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.order.domain.responses.OrderResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt;
import com.sanedge.pointofsale.order.models.Order;
import com.sanedge.pointofsale.order.repository.OrderQueryRepository;
import com.sanedge.pointofsale.order.service.OrderQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderQueryImplService implements OrderQueryService {
        private final OrderQueryRepository orderQueryRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<OrderResponse>> findAll(FindAllOrderRequest req) {
                return tracingMetrics.traceAndMeasure("findAllOrders", "find_all_orders", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Searching all orders | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Order> orderPage = orderQueryRepository.findOrders(keyword, pageable);

                                List<OrderResponse> responses = orderPage.getContent()
                                                .stream()
                                                .map(OrderResponse::from)
                                                .toList();

                                log.info("✅ Found {} orders", responses.size());

                                return ApiResponsePagination.<List<OrderResponse>>builder()
                                                .status("success")
                                                .message("Orders retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(orderPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch orders", e);
                                return ApiResponsePagination.<List<OrderResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch orders")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<OrderResponseDeleteAt>> findByActive(FindAllOrderRequest req) {
                return tracingMetrics.traceAndMeasure("findActiveOrders", "find_active_orders", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Searching active orders | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Order> orderPage = orderQueryRepository.findActiveOrders(keyword, pageable);

                                List<OrderResponseDeleteAt> responses = orderPage.getContent()
                                                .stream()
                                                .map(OrderResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} active orders", responses.size());

                                return ApiResponsePagination.<List<OrderResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Active orders retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(orderPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch active orders", e);
                                return ApiResponsePagination.<List<OrderResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to fetch active orders")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<OrderResponseDeleteAt>> findByTrashed(FindAllOrderRequest req) {
                return tracingMetrics.traceAndMeasure("findTrashedOrders", "find_trashed_orders", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Searching trashed orders | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Order> orderPage = orderQueryRepository.findTrashedOrders(keyword, pageable);

                                List<OrderResponseDeleteAt> responses = orderPage.getContent()
                                                .stream()
                                                .map(OrderResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} trashed orders", responses.size());

                                return ApiResponsePagination.<List<OrderResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Trashed orders retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(orderPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch trashed orders", e);
                                return ApiResponsePagination.<List<OrderResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to fetch trashed orders")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<OrderResponse>> findByMerchantId(FindAllOrderByMerchantRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("merchant.id",
                                                req.getMerchantId() != null ? req.getMerchantId().toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findOrdersByMerchant", "find_orders_by_merchant", attrs, () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Searching orders by merchantId={} | Page: {}, Size: {}, Search: {}", req.getMerchantId(),
                                        page + 1,
                                        pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Order> orderPage = orderQueryRepository.findOrdersByMerchant(
                                                req.getMerchantId().longValue(),
                                                keyword,
                                                pageable);

                                List<OrderResponse> responses = orderPage.getContent()
                                                .stream()
                                                .map(OrderResponse::from)
                                                .toList();

                                log.info("✅ Found {} orders for merchantId={}", responses.size(), req.getMerchantId());

                                return ApiResponsePagination.<List<OrderResponse>>builder()
                                                .status("success")
                                                .message("Orders retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(orderPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch orders for merchantId={}", req.getMerchantId(), e);
                                return ApiResponsePagination.<List<OrderResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch orders for merchant")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<OrderResponse> findById(Integer id) {
                Attributes attrs = Attributes.builder()
                                .put("order.id", String.valueOf(id))
                                .build();

                return tracingMetrics.traceAndMeasure("findOrderById", "find_order_by_id", attrs, () -> {
                        log.info("🔍 Finding order by id={}", id);
                        try {
                                return orderQueryRepository.findOrderById(id.longValue())
                                                .map(order -> ApiResponse.<OrderResponse>builder()
                                                                .status("success")
                                                                .message("Order retrieved successfully")
                                                                .data(OrderResponse.from(order))
                                                                .build())
                                                .orElseGet(() -> {
                                                        log.warn("❌ Order not found with id={}", id);
                                                        return ApiResponse.<OrderResponse>builder()
                                                                        .status("error")
                                                                        .message("Order not found")
                                                                        .data(null)
                                                                        .build();
                                                });
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch order by id={}", id, e);
                                return ApiResponse.<OrderResponse>builder()
                                                .status("error")
                                                .message("Failed to fetch order")
                                                .data(null)
                                                .build();
                        }
                });
        }
}
