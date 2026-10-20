package com.sanedge.pointofsale.cashier.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.cashier.domain.requests.FindAllCashierMerchant;
import com.sanedge.pointofsale.cashier.domain.requests.FindAllCashiers;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.domain.responses.api.PaginationMeta;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt;
import com.sanedge.pointofsale.cashier.models.Cashier;
import com.sanedge.pointofsale.cashier.repository.CashierQueryRepository;
import com.sanedge.pointofsale.cashier.service.CashierQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CashierQueryImplService implements CashierQueryService {
        private final CashierQueryRepository cashierQueryRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<CashierResponse>> findAll(FindAllCashiers req) {
                return tracingMetrics.traceAndMeasure("findAllCashiers", "find_all_cashiers", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Searching all cashiers | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Cashier> cashierPage = cashierQueryRepository.findAllCashiers(keyword, pageable);

                                List<CashierResponse> responses = cashierPage.getContent()
                                                .stream()
                                                .map(CashierResponse::from)
                                                .toList();

                                log.info("✅ Found {} cashiers", responses.size());

                                return ApiResponsePagination.<List<CashierResponse>>builder()
                                                .status("success")
                                                .message("Cashiers retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(cashierPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch cashiers", e);
                                return ApiResponsePagination.<List<CashierResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch cashiers")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<CashierResponse> findById(Integer cashierId) {
                Attributes attrs = Attributes.builder()
                                .put("cashier.id", cashierId != null ? cashierId.toString() : "null")
                                .build();

                return tracingMetrics.traceAndMeasure("findCashierById", "find_cashier_by_id", attrs, () -> {
                        log.info("🔍 Finding cashier by id={}", cashierId);
                        try {
                                return cashierQueryRepository.findById(cashierId.longValue())
                                                .map(cashier -> ApiResponse.<CashierResponse>builder()
                                                                .status("success")
                                                                .message("Cashier retrieved successfully")
                                                                .data(CashierResponse.from(cashier))
                                                                .build())
                                                .orElseGet(() -> {
                                                        log.warn("❌ Cashier not found with id={}", cashierId);
                                                        return ApiResponse.<CashierResponse>builder()
                                                                        .status("error")
                                                                        .message("Cashier not found")
                                                                        .data(null)
                                                                        .build();
                                                });
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch cashier by id={}", cashierId, e);
                                return ApiResponse.<CashierResponse>builder()
                                                .status("error")
                                                .message("Failed to fetch cashier")
                                                .data(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<CashierResponseDeleteAt>> findByActive(FindAllCashiers req) {
                return tracingMetrics.traceAndMeasure("findActiveCashiers", "find_active_cashiers", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Searching active cashiers | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Cashier> cashierPage = cashierQueryRepository.findActiveCashiers(keyword, pageable);

                                List<CashierResponseDeleteAt> responses = cashierPage.getContent()
                                                .stream()
                                                .map(CashierResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} active cashiers", responses.size());

                                return ApiResponsePagination.<List<CashierResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Active cashiers retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(cashierPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch active cashiers", e);
                                return ApiResponsePagination.<List<CashierResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to fetch active cashiers")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<CashierResponseDeleteAt>> findByTrashed(FindAllCashiers req) {
                return tracingMetrics.traceAndMeasure("findTrashedCashiers", "find_trashed_cashiers", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Searching trashed cashiers | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Cashier> cashierPage = cashierQueryRepository.findTrashedCashiers(keyword, pageable);

                                List<CashierResponseDeleteAt> responses = cashierPage.getContent()
                                                .stream()
                                                .map(CashierResponseDeleteAt::from)
                                                .toList();

                                log.info("✅ Found {} trashed cashiers", responses.size());

                                return ApiResponsePagination.<List<CashierResponseDeleteAt>>builder()
                                                .status("success")
                                                .message("Trashed cashiers retrieved successfully")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(cashierPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch trashed cashiers", e);
                                return ApiResponsePagination.<List<CashierResponseDeleteAt>>builder()
                                                .status("error")
                                                .message("Failed to fetch trashed cashiers")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponsePagination<List<CashierResponse>> findByMerchant(FindAllCashierMerchant req) {
                return tracingMetrics.traceAndMeasure("findByMerchantCashiers", "find_by_merchant_cashiers", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Finding cashiers by merchant_id={} | Page: {}, Size: {}", req.getMerchantId(), page + 1,
                                        pageSize);

                        try {
                                Pageable pageable = PageRequest.of(page, pageSize);
                                Page<Cashier> cashierPage = cashierQueryRepository.findByMerchants(
                                                req.getMerchantId().longValue(), keyword,
                                                pageable);

                                List<CashierResponse> responses = cashierPage.getContent()
                                                .stream()
                                                .map(CashierResponse::from)
                                                .toList();

                                log.info("✅ Found {} cashiers for merchant_id={}", responses.size(), req.getMerchantId());

                                return ApiResponsePagination.<List<CashierResponse>>builder()
                                                .status("success")
                                                .message("Cashiers retrieved successfully by merchant")
                                                .data(responses)
                                                .pagination(PaginationMeta.fromSpringPage(cashierPage))
                                                .build();
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch cashiers by merchant_id={}", req.getMerchantId(), e);
                                return ApiResponsePagination.<List<CashierResponse>>builder()
                                                .status("error")
                                                .message("Failed to fetch cashiers by merchant")
                                                .data(Collections.emptyList())
                                                .pagination(null)
                                                .build();
                        }
                });
        }
}
