package com.sanedge.pointofsale.order.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.adapter.cashier.CashierQueryPort;
import com.sanedge.pointofsale.common.adapter.merchant.MerchantQueryPort;
import com.sanedge.pointofsale.common.adapter.model.CreateOrderItemCmd;
import com.sanedge.pointofsale.common.adapter.model.Product;
import com.sanedge.pointofsale.common.adapter.model.UpdateOrderItemCmd;
import com.sanedge.pointofsale.common.adapter.model.UpdateProductCmd;
import com.sanedge.pointofsale.common.adapter.order_item.OrderItemCommandPort;
import com.sanedge.pointofsale.common.adapter.product.ProductCommandPort;
import com.sanedge.pointofsale.common.adapter.product.ProductQueryPort;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.order.domain.requests.CreateOrderItemRequest;
import com.sanedge.pointofsale.order.domain.requests.CreateOrderRequest;
import com.sanedge.pointofsale.order.domain.requests.UpdateOrderItemRequest;
import com.sanedge.pointofsale.order.domain.requests.UpdateOrderRequest;
import com.sanedge.pointofsale.order.domain.responses.OrderResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt;
import com.sanedge.pointofsale.order.models.Order;
import com.sanedge.pointofsale.order.repository.OrderCommandRepository;
import com.sanedge.pointofsale.order.repository.OrderQueryRepository;
import com.sanedge.pointofsale.order.service.OrderCommandService;

import io.opentelemetry.api.common.Attributes;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderCommandImplService implements OrderCommandService {
    private final OrderQueryRepository orderQueryRepository;
    private final OrderCommandRepository orderCommandRepository;
    private final Validator validator;
    private final MerchantQueryPort merchantPort;
    private final CashierQueryPort cashierPort;
    private final ProductQueryPort productPort;
    private final ProductCommandPort productCommandPort;
    private final OrderItemCommandPort orderItemPort;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<OrderResponse> create(CreateOrderRequest request) {
        Attributes attrs = Attributes.builder()
                .put("merchant.id", request.getMerchantId() != null ? request.getMerchantId().toString() : "null")
                .put("cashier.id", request.getCashierId() != null ? request.getCashierId().toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("createOrder", "create_order", attrs, () -> {
            try {
                log.info("🆕 Creating new order for merchantId={} and cashierId={}", request.getMerchantId(),
                        request.getCashierId());

                validateRequest(request);

                ensureMerchant(request.getMerchantId());
                ensureCashier(request.getCashierId());

                Order order = new Order();
                order.setMerchantId(request.getMerchantId().longValue());
                order.setCashierId(request.getCashierId().longValue());
                order.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                order.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                order.setTotalPrice(0L);
                order = orderCommandRepository.save(order);

                int totalPrice = 0;

                for (CreateOrderItemRequest itemReq : request.getItems()) {
                    Product product = findProduct(itemReq.getProductId());

                    if (product.countInStock() < itemReq.getQuantity()) {
                        throw new IllegalArgumentException(
                                "Insufficient stock for product id=" + itemReq.getProductId());
                    }

                    orderItemPort.createOrderItem(new CreateOrderItemCmd(
                            order.getOrderId().intValue(),
                            itemReq.getProductId(),
                            itemReq.getQuantity(),
                            itemReq.getPrice()));

                    updateProductStock(product, itemReq.getQuantity());

                    totalPrice += itemReq.getQuantity() * itemReq.getPrice();
                }

                order.setTotalPrice(Long.valueOf(totalPrice));
                orderCommandRepository.save(order);

                log.info("✅ Order created successfully with id={}", order.getOrderId());
                return ApiResponse.<OrderResponse>builder()
                        .status("success")
                        .message("Order created successfully")
                        .data(OrderResponse.from(order))
                        .build();

            } catch (ResourceNotFoundException e) {
                throw e;
            } catch (Exception e) {
                log.error("💥 Failed to create order", e);
                return ApiResponse.<OrderResponse>builder()
                        .status("error")
                        .message("Failed to create your order. Please try again later.")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<OrderResponse> update(UpdateOrderRequest request) {
        Attributes attrs = Attributes.builder()
                .put("order.id", request.getOrderId() != null ? request.getOrderId().toString() : "null")
                .build();

        return tracingMetrics.traceAndMeasure("updateOrder", "update_order", attrs, () -> {
            try {
                log.info("🔄 Updating order id={}", request.getOrderId());

                Order order = orderQueryRepository.findOrderById(request.getOrderId().longValue())
                        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

                ensureCashier(request.getCashierId());

                int totalPrice = 0;

                for (UpdateOrderItemRequest itemReq : request.getItems()) {
                    Product product = findProduct(itemReq.getProductId());

                    if (itemReq.getOrderItemId() != null && itemReq.getOrderItemId() > 0) {
                        orderItemPort.updateOrderItem(new UpdateOrderItemCmd(
                                itemReq.getOrderItemId(),
                                order.getOrderId().intValue(),
                                itemReq.getProductId(),
                                itemReq.getQuantity(),
                                itemReq.getPrice()));
                    } else {
                        if (product.countInStock() < itemReq.getQuantity()) {
                            throw new IllegalArgumentException(
                                    "Insufficient stock for product id=" + itemReq.getProductId());
                        }
                        orderItemPort.createOrderItem(new CreateOrderItemCmd(
                                order.getOrderId().intValue(),
                                itemReq.getProductId(),
                                itemReq.getQuantity(),
                                itemReq.getPrice()));

                        updateProductStock(product, itemReq.getQuantity());
                    }

                    totalPrice += itemReq.getQuantity() * itemReq.getPrice();
                }

                order.setTotalPrice(Long.valueOf(totalPrice));
                orderCommandRepository.save(order);

                log.info("✅ Order updated successfully id={}", order.getOrderId());
                return ApiResponse.<OrderResponse>builder()
                        .status("success")
                        .message("Order updated successfully")
                        .data(OrderResponse.from(order))
                        .build();

            } catch (ResourceNotFoundException e) {
                throw e;
            } catch (Exception e) {
                log.error("💥 Failed to update order id={}", request.getOrderId(), e);
                return ApiResponse.<OrderResponse>builder()
                        .status("error")
                        .message("Failed to update your order. Please try again later.")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<OrderResponseDeleteAt> trash(Integer id) {
        Attributes attrs = Attributes.builder()
                .put("order.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("trashOrder", "trash_order", attrs, () -> {
            log.info("🗑️ Trashing order id={}", id);
            try {
                Order order = orderCommandRepository.trashed(id.longValue());

                return ApiResponse.<OrderResponseDeleteAt>builder()
                        .status("success")
                        .message("🗑️ Order trashed successfully!")
                        .data(OrderResponseDeleteAt.from(order))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash order id={}", id, e);
                return ApiResponse.<OrderResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to trash order: " + e.getMessage())
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<OrderResponseDeleteAt> restore(Integer id) {
        Attributes attrs = Attributes.builder()
                .put("order.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("restoreOrder", "restore_order", attrs, () -> {
            log.info("♻️ Restoring order id={}", id);
            try {
                Order order = orderCommandRepository.restore(id.longValue());
                return ApiResponse.<OrderResponseDeleteAt>builder()
                        .status("success")
                        .message("♻️ Order restored successfully!")
                        .data(OrderResponseDeleteAt.from(order))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore order id={}", id, e);
                return ApiResponse.<OrderResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to restore order: " + e.getMessage())
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> delete(Integer id) {
        Attributes attrs = Attributes.builder()
                .put("order.id", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("deleteOrderPermanent", "delete_order_permanent", attrs, () -> {
            log.info("🧨 Permanently deleting order id={}", id);
            try {
                orderCommandRepository.deletePermanent(id.longValue());
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🧨 Order permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete order id={}", id, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to permanently delete order: " + e.getMessage())
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllOrders", "restore_all_orders", () -> {
            log.info("🔄 Restoring ALL trashed orders");
            try {
                orderCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🔄 All orders restored successfully!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all orders", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to restore all orders: " + e.getMessage())
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        return tracingMetrics.traceAndMeasure("deleteAllOrders", "delete_all_orders", () -> {
            log.info("💣 Permanently deleting ALL trashed orders");
            try {
                orderCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("💣 All orders permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all orders", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to delete all orders: " + e.getMessage())
                        .data(false)
                        .build();
            }
        });
    }

    private void ensureMerchant(Integer merchantId) {
        merchantPort.findByMerchantId(merchantId);
    }

    private void ensureCashier(Integer cashierId) {
        cashierPort.findById(cashierId);
    }

    private Product findProduct(Integer productId) {
        return productPort.findById(productId);
    }

    private void updateProductStock(Product product, int quantity) {
        productCommandPort.update(new UpdateProductCmd(
                product.id(),
                product.merchantId(),
                product.categoryId(),
                product.name(),
                product.description(),
                product.price(),
                product.countInStock() - quantity,
                product.brand(),
                product.weight(),
                product.rating(),
                product.slugProduct(),
                product.imageProduct(),
                product.barcode()));
    }

    private <T> void validateRequest(T req) {
        Set<ConstraintViolation<T>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
            }
            log.error("Validation failed: {}", sb);
            throw new ConstraintViolationException("Validation failed: " + sb, violations);
        }
    }

}
