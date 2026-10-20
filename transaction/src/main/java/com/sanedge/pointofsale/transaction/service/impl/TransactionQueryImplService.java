package com.sanedge.pointofsale.transaction.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.transaction.domain.requests.FindAllTransactionByMerchantRequest;
import com.sanedge.pointofsale.transaction.domain.requests.FindAllTransactionRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.domain.responses.api.PaginationMeta;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionResponseDeleteAt;
import com.sanedge.pointofsale.transaction.models.Transaction;
import com.sanedge.pointofsale.transaction.repository.TransactionQueryRepository;
import com.sanedge.pointofsale.transaction.service.TransactionQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionQueryImplService implements TransactionQueryService {

        private final TransactionQueryRepository transactionQueryRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<TransactionResponse>> findAllTransactions(FindAllTransactionRequest req) {
                return tracingMetrics.traceAndMeasure("findAllTransactions", "find_all_transactions", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null) ? req.getSearch() : "";

                        log.info("🔍 Searching all transactions | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Transaction> transactionPage = transactionQueryRepository.findTransactions(keyword,
                                                pageable);

                                List<TransactionResponse> responses = transactionPage.getContent()
                                                .stream()
                                                .map(TransactionResponse::from)
                                                .toList();

                                log.info("✅ Found {} transactions", responses.size());

                                return ApiResponsePagination.<List<TransactionResponse>>builder()
                                                .status("success")
                                                .message("Transactions retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(transactionPage))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch transactions", e);
                                return ApiResponsePagination.<List<TransactionResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch transactions")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<TransactionResponseDeleteAt>> findByActive(FindAllTransactionRequest req) {
                return tracingMetrics.traceAndMeasure("findActiveTransactions", "find_active_transactions", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null) ? req.getSearch() : "";

                        log.info("🔍 Searching active transactions | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Transaction> transactionPage = transactionQueryRepository.findActiveTransactions(keyword,
                                                pageable);

                                List<TransactionResponseDeleteAt> responses = transactionPage.getContent()
                                                .stream()
                                                .map(TransactionResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} active transactions", responses.size());

                                return ApiResponsePagination.<List<TransactionResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Active transactions retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(transactionPage))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch active transactions", e);
                                return ApiResponsePagination.<List<TransactionResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to fetch active transactions")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<TransactionResponseDeleteAt>> findByTrashed(FindAllTransactionRequest req) {
                return tracingMetrics.traceAndMeasure("findTrashedTransactions", "find_trashed_transactions", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null) ? req.getSearch() : "";

                        log.info("🔍 Searching trashed transactions | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Transaction> transactionPage = transactionQueryRepository.findTrashedTransactions(keyword,
                                                pageable);

                                List<TransactionResponseDeleteAt> responses = transactionPage.getContent()
                                                .stream()
                                                .map(TransactionResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} trashed transactions", responses.size());

                                return ApiResponsePagination.<List<TransactionResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Trashed transactions retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(transactionPage))
                                                .build();

                        } catch (Exception e) {
                                log.error("💥 Failed to fetch trashed transactions", e);
                                return ApiResponsePagination.<List<TransactionResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to fetch trashed transactions")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<TransactionResponse>> findByMerchant(
                        FindAllTransactionByMerchantRequest req) {
                Attributes attrs = Attributes.builder()
                                .put("merchantId", req.getMerchantId() != null ? req.getMerchantId().longValue() : 0L)
                                .build();

                return tracingMetrics.traceAndMeasure("findTransactionsByMerchant", "find_transactions_by_merchant",
                                attrs, () -> {
                                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                                        String keyword = (req.getSearch() != null) ? req.getSearch() : "";

                                        log.info("🔍 Searching transactions by merchant_id={} | Page: {}, Size: {}, Search: {}",
                                                        req.getMerchantId(),
                                                        page + 1, pageSize, keyword.isEmpty() ? "None" : keyword);

                                        try {
                                                Pageable pageable = PageRequest.of(page, pageSize);
                                                Page<Transaction> transactionPage = transactionQueryRepository.findTransactionsByMerchant(
                                                                keyword,
                                                                req.getMerchantId(), pageable);

                                                List<TransactionResponse> responses = transactionPage.getContent()
                                                                .stream()
                                                                .map(TransactionResponse::from)
                                                                .toList();

                                                log.info("✅ Found {} transactions for merchant_id={}", responses.size(), req.getMerchantId());

                                                return ApiResponsePagination.<List<TransactionResponse>>builder()
                                                                .status("success")
                                                                .message("Transactions by merchant retrieved successfully")
                                                                .data(responses)
                                                                .pagination(PaginationMeta.fromSpringPage(transactionPage))
                                                                .build();

                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch transactions by merchant_id={}", req.getMerchantId(), e);
                                                return ApiResponsePagination.<List<TransactionResponse>>builder()
                                                                .status("error")
                                                                .message("Failed to fetch transactions by merchant")
                                                                .data(Collections.emptyList())
                                                                .pagination(null)
                                                                .build();
                                        }
                                });
        }

        @Override
        public ApiResponse<TransactionResponse> findById(Integer id) {
                Attributes attrs = Attributes.builder()
                                .put("transactionId", String.valueOf(id))
                                .build();

                return tracingMetrics.traceAndMeasure("findTransactionById", "find_transaction_by_id", attrs, () -> {
                        log.info("🔍 Finding transaction by id={}", id);
                        try {
                                return transactionQueryRepository.findById(id.longValue())
                                                .map(tx -> ApiResponse.<TransactionResponse>builder()
                                                                .status("success")
                                                                .message("Transaction retrieved successfully")
                                                                .data(TransactionResponse.from(tx))
                                                                .build())
                                                .orElseGet(() -> {
                                                        log.warn("❌ Transaction not found with id={}", id);
                                                        return ApiResponse.<TransactionResponse>builder()
                                                                        .status("error")
                                                                        .message("Transaction not found")
                                                                        .data(null)
                                                                        .build();
                                                });
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch transaction by id={}", id, e);
                                return ApiResponse.<TransactionResponse>builder()
                                                .status("error")
                                                .message("Failed to fetch transaction")
                                                .data(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<TransactionResponse> findByOrderId(Integer id) {
                Attributes attrs = Attributes.builder()
                                .put("orderId", String.valueOf(id))
                                .build();

                return tracingMetrics.traceAndMeasure("findTransactionByOrderId", "find_transaction_by_order_id", attrs,
                                () -> {
                                        log.info("🔍 Finding transaction by order_id={}", id);
                                        try {
                                                return transactionQueryRepository.findByOrderId(id.longValue())
                                                                .map(tx -> ApiResponse.<TransactionResponse>builder()
                                                                                .status("success")
                                                                                .message("Transaction retrieved successfully")
                                                                                .data(TransactionResponse.from(tx))
                                                                                .build())
                                                                .orElseGet(() -> {
                                                                        log.warn("❌ Transaction not found with order_id={}", id);
                                                                        return ApiResponse.<TransactionResponse>builder()
                                                                                        .status("error")
                                                                                        .message("Transaction not found for order")
                                                                                        .data(null)
                                                                                        .build();
                                                                });
                                        } catch (Exception e) {
                                                log.error("💥 Failed to fetch transaction by order_id={}", id, e);
                                                return ApiResponse.<TransactionResponse>builder()
                                                                .status("error")
                                                                .message("Failed to fetch transaction by order")
                                                                .data(null)
                                                                .build();
                                        }
                                });
        }
}
