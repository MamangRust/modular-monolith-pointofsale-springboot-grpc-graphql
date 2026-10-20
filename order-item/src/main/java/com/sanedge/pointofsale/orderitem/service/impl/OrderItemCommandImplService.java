package com.sanedge.pointofsale.orderitem.service.impl;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.orderitem.domain.requests.CreateOrderItemRequest;
import com.sanedge.pointofsale.orderitem.domain.requests.UpdateOrderItemRequest;
import com.sanedge.pointofsale.orderitem.domain.responses.OrderItemResponse;
import com.sanedge.pointofsale.orderitem.models.OrderItem;
import com.sanedge.pointofsale.orderitem.repository.OrderItemRepository;
import com.sanedge.pointofsale.orderitem.service.OrderItemCommandService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderItemCommandImplService implements OrderItemCommandService {

    private final OrderItemRepository orderItemRepository;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<OrderItemResponse> create(CreateOrderItemRequest request) {
        Attributes attrs = Attributes.builder()
                .put("order.id", request.getOrderId() != null ? request.getOrderId().toString() : "null")
                .put("product.id", request.getProductId() != null ? request.getProductId().toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("createOrderItem", "create_order_item", attrs, () -> {
            log.info("🆕 Creating order item for orderId={} productId={}", request.getOrderId(),
                    request.getProductId());
            try {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(request.getOrderId().longValue());
                orderItem.setProductId(request.getProductId().longValue());
                orderItem.setQuantity(request.getQuantity());
                orderItem.setPrice(request.getPrice());
                orderItem = orderItemRepository.save(orderItem);

                log.info("✅ Order item created successfully with id={}", orderItem.getOrderItemId());
                return ApiResponse.<OrderItemResponse>builder()
                        .status("success")
                        .message("Order item created successfully")
                        .data(OrderItemResponse.from(orderItem))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to create order item", e);
                return ApiResponse.<OrderItemResponse>builder()
                        .status("error")
                        .message("Failed to create order item. Please try again later.")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<OrderItemResponse> update(UpdateOrderItemRequest request) {
        Attributes attrs = Attributes.builder()
                .put("order_item.id",
                        request.getOrderItemId() != null ? request.getOrderItemId().toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("updateOrderItem", "update_order_item", attrs, () -> {
            log.info("🔄 Updating order item id={}", request.getOrderItemId());
            try {
                OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId().longValue())
                        .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

                orderItem.setOrderId(request.getOrderId().longValue());
                orderItem.setProductId(request.getProductId().longValue());
                orderItem.setQuantity(request.getQuantity());
                orderItem.setPrice(request.getPrice());
                orderItem = orderItemRepository.save(orderItem);

                log.info("✅ Order item updated successfully id={}", orderItem.getOrderItemId());
                return ApiResponse.<OrderItemResponse>builder()
                        .status("success")
                        .message("Order item updated successfully")
                        .data(OrderItemResponse.from(orderItem))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to update order item id={}", request.getOrderItemId(), e);
                return ApiResponse.<OrderItemResponse>builder()
                        .status("error")
                        .message("Failed to update order item. Please try again later.")
                        .data(null)
                        .build();
            }
        });
    }
}
