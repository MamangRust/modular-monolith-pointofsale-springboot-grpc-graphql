package com.sanedge.pointofsale.orderitem.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.orderitem.domain.responses.OrderItemResponse;
import com.sanedge.pointofsale.orderitem.models.OrderItem;
import com.sanedge.pointofsale.orderitem.repository.OrderItemRepository;
import com.sanedge.pointofsale.orderitem.service.OrderItemQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderItemQueryImplService implements OrderItemQueryService {

    private final OrderItemRepository orderItemRepository;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<List<OrderItemResponse>> findByOrder(Integer orderId) {
        Attributes attrs = Attributes.builder()
                .put("order.id", String.valueOf(orderId))
                .build();

        return tracingMetrics.traceAndMeasure("findOrderItemByOrder", "find_order_item_by_order", attrs, () -> {
            log.info("🔍 Finding order items for orderId={}", orderId);
            try {
                List<OrderItemResponse> responses = orderItemRepository
                        .findOrderItemByOrder(orderId.longValue())
                        .stream()
                        .map(OrderItemResponse::from)
                        .toList();

                return ApiResponse.<List<OrderItemResponse>>builder()
                        .status("success")
                        .message("Order items retrieved successfully")
                        .data(responses)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to fetch order items for orderId={}: {}", orderId, e.getMessage(), e);
                return ApiResponse.<List<OrderItemResponse>>builder()
                        .status("error")
                        .message("Failed to fetch order items")
                        .data(List.of())
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<OrderItemResponse> findById(Integer id) {
        return tracingMetrics.traceAndMeasure("findOrderItemById", "find_order_item_by_id", () -> {
            log.info("🔎 Finding order item by id={}", id);
            try {
                return orderItemRepository.findById(id.longValue())
                        .map(item -> ApiResponse.<OrderItemResponse>builder()
                                .status("success")
                                .message("Order item retrieved successfully")
                                .data(OrderItemResponse.from(item))
                                .build())
                        .orElseGet(() -> ApiResponse.<OrderItemResponse>builder()
                                .status("error")
                                .message("Order item not found")
                                .data(null)
                                .build());
            } catch (Exception e) {
                log.error("💥 Failed to fetch order item by id={}: {}", id, e.getMessage(), e);
                return ApiResponse.<OrderItemResponse>builder()
                        .status("error")
                        .message("Failed to fetch order item")
                        .data(null)
                        .build();
            }
        });
    }
}
