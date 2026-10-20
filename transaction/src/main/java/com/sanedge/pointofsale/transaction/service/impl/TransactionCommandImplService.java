package com.sanedge.pointofsale.transaction.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanedge.pointofsale.common.adapter.merchant.MerchantQueryPort;
import com.sanedge.pointofsale.common.adapter.model.OrderItem;
import com.sanedge.pointofsale.common.adapter.order.OrderQueryPort;
import com.sanedge.pointofsale.common.adapter.order_item.OrderItemQueryPort;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.enums.PaymentStatus;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.common.outbox.Outbox;
import com.sanedge.pointofsale.common.outbox.OutboxEventFactory;
import com.sanedge.pointofsale.common.outbox.OutboxRepository;
import com.sanedge.pointofsale.transaction.domain.requests.CreateTransactionRequest;
import com.sanedge.pointofsale.transaction.domain.requests.UpdateTransactionRequest;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionResponseDeleteAt;
import com.sanedge.pointofsale.transaction.models.Transaction;
import com.sanedge.pointofsale.transaction.repository.TransactionCommandRepository;
import com.sanedge.pointofsale.transaction.repository.TransactionQueryRepository;
import com.sanedge.pointofsale.transaction.service.TransactionCommandService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionCommandImplService implements TransactionCommandService {

    /** The only transaction email topic the email-service consumes. */
    private static final String TOPIC_TRANSACTION_CREATE = "email-service-topic-transaction-create";

    private final TransactionQueryRepository transactionQueryRepository;
    private final TransactionCommandRepository transactionCommandRepository;
    private final OutboxRepository outboxRepository;
    private final MerchantQueryPort merchantPort;
    private final OrderQueryPort orderPort;
    private final OrderItemQueryPort orderItemPort;
    private final TracingMetrics tracingMetrics;

    /** Recipient for transaction notifications; matches the blueprint default. */
    @Value("${notification.transaction.email:admin@example.com}")
    private String notificationEmail;

    @Override
    public ApiResponse<TransactionResponse> create(CreateTransactionRequest req) {
        Attributes attrs = Attributes.builder()
                .put("merchantId", String.valueOf(req.getMerchantID()))
                .put("orderId", String.valueOf(req.getOrderID()))
                .build();

        return tracingMetrics.traceAndMeasure("createTransaction", "create_transaction", attrs, () -> {
            log.info("💳 Creating new transaction | orderId={}, merchantId={}", req.getOrderID(), req.getMerchantID());

            if (!merchantExists(req.getMerchantID())) {
                log.error("❌ Merchant not found | merchantId={}", req.getMerchantID());
                return error("Merchant not found");
            }

            if (!orderExists(req.getOrderID())) {
                log.error("❌ Order not found | orderId={}", req.getOrderID());
                return error("Order not found");
            }

            List<OrderItem> orderItems = fetchOrderItems(req.getOrderID());
            if (orderItems.isEmpty()) {
                log.error("❌ No order items found | orderId={}", req.getOrderID());
                return error("No order items found");
            }

            int totalAmount = 0;
            for (OrderItem item : orderItems) {
                if (item.quantity() <= 0) {
                    return error("Invalid order item quantity");
                }
                totalAmount += item.price() * item.quantity();
            }
            int ppn = totalAmount * 11 / 100;
            int totalAmountWithTax = totalAmount + ppn;

            String paymentStatus = req.getAmount() >= totalAmountWithTax ? "success" : "failed";
            if (paymentStatus.equals("failed")) {
                log.error("❌ Insufficient payment amount | amount={}, required={}", req.getAmount(), totalAmountWithTax);
                return error("Insufficient payment amount");
            }

            req.setAmount(totalAmountWithTax);
            req.setPaymentStatus(paymentStatus);

            try {
                Transaction transaction = new Transaction();
                transaction.setOrderId(req.getOrderID().longValue());
                transaction.setMerchantId(req.getMerchantID().longValue());
                transaction.setPaymentMethod(req.getPaymentMethod());
                transaction.setAmount(req.getAmount());

                PaymentStatus status = PaymentStatus.fromValue(req.getPaymentStatus());

                transaction.setStatus(status);

                transaction = transactionCommandRepository.save(transaction);
                log.info("✅ Transaction created successfully | transactionId={}", transaction.getTransactionId());

                persistOutboxEvent(transaction);

                return ApiResponse.<TransactionResponse>builder()
                        .status("success")
                        .message("Transaction created successfully")
                        .data(TransactionResponse.from(transaction))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to create transaction", e);
                return error("Failed to create transaction");
            }
        });
    }

    @Override
    public ApiResponse<TransactionResponse> update(UpdateTransactionRequest req) {
        Attributes attrs = Attributes.builder()
                .put("transactionId", String.valueOf(req.getTransactionID()))
                .build();

        return tracingMetrics.traceAndMeasure("updateTransaction", "update_transaction", attrs, () -> {
            log.info("✏️ Updating transaction | transactionId={}", req.getTransactionID());

            Transaction existingTx = transactionQueryRepository.findById(req.getTransactionID().longValue())
                    .orElse(null);
            if (existingTx == null) {
                log.error("❌ Transaction not found | transactionId={}", req.getTransactionID());
                return error("Transaction not found");
            }

            if (PaymentStatus.SUCCESS.equals(existingTx.getStatus()) ||
                    PaymentStatus.REFUNDED.equals(existingTx.getStatus())) {

                log.error("❌ Transaction cannot be modified | transactionId={}", req.getTransactionID());
                return error("Transaction cannot be modified");
            }

            if (!merchantExists(req.getMerchantID())) {
                log.error("❌ Merchant not found | merchantId={}", req.getMerchantID());
                return error("Merchant not found");
            }

            if (!orderExists(req.getOrderID())) {
                log.error("❌ Order not found | orderId={}", req.getOrderID());
                return error("Order not found");
            }

            List<OrderItem> orderItems = fetchOrderItems(req.getOrderID());
            if (orderItems.isEmpty()) {
                log.error("❌ No order items found | orderId={}", req.getOrderID());
                return error("No order items found");
            }

            int totalAmount = 0;
            for (OrderItem item : orderItems) {
                if (item.quantity() <= 0) {
                    return error("Invalid order item quantity");
                }
                totalAmount += item.price() * item.quantity();
            }
            int ppn = totalAmount * 11 / 100;
            int totalAmountWithTax = totalAmount + ppn;

            String paymentStatus = req.getAmount() >= totalAmountWithTax ? "success" : "failed";
            if (paymentStatus.equals("failed")) {
                log.error("❌ Insufficient payment amount | amount={}, required={}", req.getAmount(), totalAmountWithTax);
                return error("Insufficient payment amount");
            }

            req.setAmount(totalAmountWithTax);
            req.setPaymentStatus(paymentStatus);

            try {
                existingTx.setOrderId(req.getOrderID().longValue());
                existingTx.setMerchantId(req.getMerchantID().longValue());
                existingTx.setPaymentMethod(req.getPaymentMethod());
                existingTx.setAmount(req.getAmount());

                PaymentStatus status = PaymentStatus.fromValue(req.getPaymentStatus());

                existingTx.setStatus(status);

                Transaction transaction = transactionCommandRepository.save(existingTx);
                log.info("✅ Transaction updated successfully | transactionId={}", transaction.getTransactionId());
                return ApiResponse.<TransactionResponse>builder()
                        .status("success")
                        .message("Transaction updated successfully")
                        .data(TransactionResponse.from(transaction))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to update transaction", e);
                return error("Failed to update transaction");
            }
        });
    }

    @Override
    public ApiResponse<TransactionResponseDeleteAt> trash(Integer id) {
        Attributes attrs = Attributes.builder()
                .put("transactionId", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("trashTransaction", "trash_transaction", attrs, () -> {
            log.info("🗑️ Trashing transaction id={}", id);
            try {
                Transaction transaction = transactionCommandRepository.trashed(id.longValue());
                return ApiResponse.<TransactionResponseDeleteAt>builder()
                        .status("success")
                        .message("🗑️ Transaction trashed successfully!")
                        .data(TransactionResponseDeleteAt.from(transaction))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash transaction id={}", id, e);
                return ApiResponse.<TransactionResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to trash transaction. Please try again later.")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<TransactionResponseDeleteAt> restore(Integer id) {
        Attributes attrs = Attributes.builder()
                .put("transactionId", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("restoreTransaction", "restore_transaction", attrs, () -> {
            log.info("♻️ Restoring transaction id={}", id);
            try {
                Transaction transaction = transactionCommandRepository.restore(id.longValue());
                return ApiResponse.<TransactionResponseDeleteAt>builder()
                        .status("success")
                        .message("♻️ Transaction restored successfully!")
                        .data(TransactionResponseDeleteAt.from(transaction))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore transaction id={}", id, e);
                return ApiResponse.<TransactionResponseDeleteAt>builder()
                        .status("error")
                        .message("Failed to restore transaction. Please try again later.")
                        .data(null)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> delete(Integer id) {
        Attributes attrs = Attributes.builder()
                .put("transactionId", String.valueOf(id))
                .build();

        return tracingMetrics.traceAndMeasure("deleteTransaction", "delete_transaction", attrs, () -> {
            log.info("🧨 Permanently deleting transaction id={}", id);
            try {
                transactionCommandRepository.deletePermanent(id.longValue());
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🧨 Transaction permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete transaction id={}", id, e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to permanently delete transaction. Please try again later.")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllTransactions", "restore_all_transactions", () -> {
            log.info("🔄 Restoring ALL trashed transactions");
            try {
                transactionCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🔄 All transactions restored successfully!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all transactions", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to restore all transactions. Please try again later.")
                        .data(false)
                        .build();
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        return tracingMetrics.traceAndMeasure("deleteAllTransactions", "delete_all_transactions", () -> {
            log.info("💣 Permanently deleting ALL trashed transactions");
            try {
                transactionCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("💣 All transactions permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all transactions", e);
                return ApiResponse.<Boolean>builder()
                        .status("error")
                        .message("Failed to delete all transactions. Please try again later.")
                        .data(false)
                        .build();
            }
        });
    }

    private boolean merchantExists(Integer merchantId) {
        try {
            merchantPort.findByMerchantId(merchantId);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private boolean orderExists(Integer orderId) {
        try {
            orderPort.findById(orderId);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private List<OrderItem> fetchOrderItems(Integer orderId) {
        return orderItemPort.findOrderItemByOrder(orderId);
    }

    private ApiResponse<TransactionResponse> error(String message) {
        return ApiResponse.<TransactionResponse>builder()
                .status("error")
                .message(message)
                .data(null)
                .build();
    }

    /**
     * Writes the transaction notification into the shared outbox table, inside the
     * same transaction as the insert. Port of the blueprint's
     * {@code persistOutboxEvent}: the recipient comes from
     * {@code notification.transaction.email}, not the order's user.
     */
    private void persistOutboxEvent(Transaction tx) {
        if (notificationEmail == null || notificationEmail.isBlank()) {
            log.warn("notification.transaction.email not configured; skipping outbox event for transactionId={}",
                    tx.getTransactionId());
            return;
        }

        String subject = "New Transaction Created";
        String body = String.format(
                "A new transaction of <b>%d</b> using <b>%s</b> has been created. Status: <b>%s</b>.",
                tx.getAmount(), tx.getPaymentMethod(), tx.getStatus());

        OutboxEventFactory.Envelope envelope = OutboxEventFactory.email(
                TOPIC_TRANSACTION_CREATE, notificationEmail, subject, body);

        Outbox event = new Outbox();
        event.setAggregateType("TRANSACTION");
        event.setAggregateId(String.valueOf(tx.getTransactionId()));
        event.setTopic(TOPIC_TRANSACTION_CREATE);
        event.setPayload(envelope.payload());
        outboxRepository.enqueue(event);
    }
}
