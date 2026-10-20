package com.sanedge.pointofsale.merchant.service;

import java.util.List;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchantDocuments;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponseDeleteAt;

/** Query side of merchant documents. */
public interface MerchantDocumentQueryService {

    ApiResponsePagination<List<MerchantDocumentResponse>> findAll(FindAllMerchantDocuments req);

    ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>> findAllActive(FindAllMerchantDocuments req);

    ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>> findAllTrashed(FindAllMerchantDocuments req);

    ApiResponse<MerchantDocumentResponse> findById(Long documentId);
}
