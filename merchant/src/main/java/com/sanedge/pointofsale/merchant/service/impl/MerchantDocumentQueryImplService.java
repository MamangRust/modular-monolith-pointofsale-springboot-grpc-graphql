package com.sanedge.pointofsale.merchant.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.domain.responses.api.PaginationMeta;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchantDocuments;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponseDeleteAt;
import com.sanedge.pointofsale.merchant.models.MerchantDocument;
import com.sanedge.pointofsale.merchant.repository.MerchantDocumentQueryRepository;
import com.sanedge.pointofsale.merchant.service.MerchantDocumentQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MerchantDocumentQueryImplService implements MerchantDocumentQueryService {

    private final MerchantDocumentQueryRepository merchantDocumentQueryRepository;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponsePagination<List<MerchantDocumentResponse>> findAll(FindAllMerchantDocuments req) {
        Attributes attrs = Attributes.builder()
                .put("document.page", String.valueOf(req.getPage()))
                .put("document.size", String.valueOf(req.getPageSize()))
                .build();

        return tracingMetrics.traceAndMeasure("findAllMerchantDocuments", "find_all_merchant_documents", attrs, () -> {
            Pageable pageable = pageable(req);
            Page<MerchantDocument> page = merchantDocumentQueryRepository.findDocuments(keyword(req), pageable);

            List<MerchantDocumentResponse> responses = page.getContent().stream()
                    .map(MerchantDocumentResponse::from)
                    .toList();

            return ApiResponsePagination.<List<MerchantDocumentResponse>>builder()
                    .status("success")
                    .message("Merchant documents retrieved successfully")
                    .data(responses)
                    .pagination(PaginationMeta.fromSpringPage(page))
                    .build();
        });
    }

    @Override
    public ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>> findAllActive(FindAllMerchantDocuments req) {
        Attributes attrs = Attributes.builder()
                .put("document.page", String.valueOf(req.getPage()))
                .put("document.size", String.valueOf(req.getPageSize()))
                .build();

        return tracingMetrics.traceAndMeasure("findActiveMerchantDocuments", "find_active_merchant_documents", attrs,
                () -> {
                    Pageable pageable = pageable(req);
                    Page<MerchantDocument> page = merchantDocumentQueryRepository.findActiveDocuments(keyword(req),
                            pageable);

                    List<MerchantDocumentResponseDeleteAt> responses = page.getContent().stream()
                            .map(MerchantDocumentResponseDeleteAt::from)
                            .toList();

                    return ApiResponsePagination.<List<MerchantDocumentResponseDeleteAt>>builder()
                            .status("success")
                            .message("Active merchant documents retrieved successfully")
                            .data(responses)
                            .pagination(PaginationMeta.fromSpringPage(page))
                            .build();
                });
    }

    @Override
    public ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>> findAllTrashed(FindAllMerchantDocuments req) {
        Attributes attrs = Attributes.builder()
                .put("document.page", String.valueOf(req.getPage()))
                .put("document.size", String.valueOf(req.getPageSize()))
                .build();

        return tracingMetrics.traceAndMeasure("findTrashedMerchantDocuments", "find_trashed_merchant_documents", attrs,
                () -> {
                    Pageable pageable = pageable(req);
                    Page<MerchantDocument> page = merchantDocumentQueryRepository.findTrashedDocuments(keyword(req),
                            pageable);

                    List<MerchantDocumentResponseDeleteAt> responses = page.getContent().stream()
                            .map(MerchantDocumentResponseDeleteAt::from)
                            .toList();

                    return ApiResponsePagination.<List<MerchantDocumentResponseDeleteAt>>builder()
                            .status("success")
                            .message("Trashed merchant documents retrieved successfully")
                            .data(responses)
                            .pagination(PaginationMeta.fromSpringPage(page))
                            .build();
                });
    }

    @Override
    public ApiResponse<MerchantDocumentResponse> findById(Long documentId) {
        Attributes attrs = Attributes.builder()
                .put("document.id", String.valueOf(documentId))
                .build();

        return tracingMetrics.traceAndMeasure("findMerchantDocumentById", "find_merchant_document_by_id", attrs, () -> {
            log.info("🔍 Fetching merchant document by ID: {}", documentId);
            return merchantDocumentQueryRepository.findDocumentById(documentId)
                    .map(doc -> ApiResponse.<MerchantDocumentResponse>builder()
                            .status("success")
                            .message("Merchant document retrieved successfully")
                            .data(MerchantDocumentResponse.from(doc))
                            .build())
                    .orElseGet(() -> ApiResponse.<MerchantDocumentResponse>builder()
                            .status("error")
                            .message("Merchant document not found")
                            .data(null)
                            .build());
        });
    }

    private static Pageable pageable(FindAllMerchantDocuments req) {
        int pageIndex = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int size = req.getPageSize() > 0 ? req.getPageSize() : 10;
        return PageRequest.of(pageIndex, size);
    }

    private static String keyword(FindAllMerchantDocuments req) {
        return (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : null;
    }
}
