package com.sanedge.pointofsale.merchant.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchants;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.domain.responses.api.PaginationMeta;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantResponseDeleteAt;
import com.sanedge.pointofsale.merchant.models.Merchant;
import com.sanedge.pointofsale.merchant.repository.MerchantQueryRepository;
import com.sanedge.pointofsale.merchant.service.MerchantQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantQueryImplService implements MerchantQueryService {

        private final MerchantQueryRepository merchantQueryRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<MerchantResponse>> findAll(FindAllMerchants req) {
                return tracingMetrics.traceAndMeasure("findAllMerchants", "find_all_merchants", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Fetching merchants | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        Pageable pageable = PageRequest.of(page, pageSize);
                        Page<Merchant> merchantPage = merchantQueryRepository.findMerchants(keyword, pageable);

                        List<MerchantResponse> responses = merchantPage.getContent()
                                        .stream()
                                        .map(MerchantResponse::from)
                                        .toList();

                        log.info("✅ Found {} merchants", responses.size());

                        return ApiResponsePagination.<List<MerchantResponse>>builder()
                                        .status("success")
                                        .message("Merchants retrieved successfully")
                                        .data(responses)
                                        .pagination(PaginationMeta.fromSpringPage(merchantPage))
                                        .build();
                });
        }

        @Override
        public ApiResponsePagination<List<MerchantResponseDeleteAt>> findByActive(FindAllMerchants req) {
                return tracingMetrics.traceAndMeasure("findActiveMerchants", "find_active_merchants", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Fetching active merchants | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        Pageable pageable = PageRequest.of(page, pageSize);
                        Page<Merchant> merchantPage = merchantQueryRepository.findActiveMerchants(keyword, pageable);

                        List<MerchantResponseDeleteAt> responses = merchantPage.getContent()
                                        .stream()
                                        .map(MerchantResponseDeleteAt::from)
                                        .toList();

                        log.info("✅ Found {} active merchants", responses.size());

                        return ApiResponsePagination.<List<MerchantResponseDeleteAt>>builder()
                                        .status("success")
                                        .message("Active merchants retrieved successfully")
                                        .data(responses)
                                        .pagination(PaginationMeta.fromSpringPage(merchantPage))
                                        .build();
                });
        }

        @Override
        public ApiResponsePagination<List<MerchantResponseDeleteAt>> findByTrashed(FindAllMerchants req) {
                return tracingMetrics.traceAndMeasure("findTrashedMerchants", "find_trashed_merchants", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Fetching trashed merchants | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        Pageable pageable = PageRequest.of(page, pageSize);
                        Page<Merchant> merchantPage = merchantQueryRepository.findTrashedMerchants(keyword, pageable);

                        List<MerchantResponseDeleteAt> responses = merchantPage.getContent()
                                        .stream()
                                        .map(MerchantResponseDeleteAt::from)
                                        .toList();

                        log.info("✅ Found {} trashed merchants", responses.size());

                        return ApiResponsePagination.<List<MerchantResponseDeleteAt>>builder()
                                        .status("success")
                                        .message("Trashed merchants retrieved successfully")
                                        .data(responses)
                                        .pagination(PaginationMeta.fromSpringPage(merchantPage))
                                        .build();
                });
        }

        @Override
        public ApiResponse<MerchantResponse> findById(Integer merchantId) {
                Attributes attrs = Attributes.builder()
                                .put("merchant.id", String.valueOf(merchantId))
                                .build();

                return tracingMetrics.traceAndMeasure("findMerchantById", "find_merchant_by_id", attrs, () -> {
                        log.info("🔍 Fetching merchant by ID: {}", merchantId);
                        try {
                                return merchantQueryRepository.findMerchantById(merchantId.longValue())
                                                .map(merchant -> ApiResponse.<MerchantResponse>builder()
                                                                .status("success")
                                                                .message("Merchant retrieved successfully")
                                                                .data(MerchantResponse.from(merchant))
                                                                .build())
                                                .orElseGet(() -> {
                                                        log.warn("❌ Merchant not found with ID: {}", merchantId);
                                                        return ApiResponse.<MerchantResponse>builder()
                                                                        .status("error")
                                                                        .message("Merchant not found")
                                                                        .data(null)
                                                                        .build();
                                                });
                        } catch (Exception e) {
                                log.error("💥 Failed to fetch merchant by ID: {}", merchantId, e);
                                return ApiResponse.<MerchantResponse>builder()
                                                .status("error")
                                                .message("Failed to fetch merchant")
                                                .data(null)
                                                .build();
                        }
                });
        }

        @Override
        public ApiResponse<List<MerchantResponse>> findByUserId(Integer userId) {
                Attributes attrs = Attributes.builder()
                                .put("user.id", String.valueOf(userId))
                                .build();

                return tracingMetrics.traceAndMeasure("findMerchantsByUserId", "find_merchants_by_user_id", attrs, () -> {
                        log.info("🔍 Fetching merchant by user ID: {}", userId);

                        List<MerchantResponse> responses = merchantQueryRepository.findByUserId(userId.longValue())
                                        .map(merchant -> List.of(MerchantResponse.from(merchant)))
                                        .orElseGet(List::of);

                        return ApiResponse.<List<MerchantResponse>>builder()
                                        .status("success")
                                        .message("Merchants retrieved successfully")
                                        .data(responses)
                                        .build();
                });
        }
}
