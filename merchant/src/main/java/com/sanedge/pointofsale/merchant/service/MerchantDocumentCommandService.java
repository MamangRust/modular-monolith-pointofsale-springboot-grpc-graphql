package com.sanedge.pointofsale.merchant.service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.merchant.domain.requests.CreateMerchantDocumentRequest;
import com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantDocumentRequest;
import com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantDocumentStatus;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantDocumentResponseDeleteAt;

/** Command side of merchant documents. */
public interface MerchantDocumentCommandService {

    ApiResponse<MerchantDocumentResponse> create(CreateMerchantDocumentRequest req);

    ApiResponse<MerchantDocumentResponse> update(UpdateMerchantDocumentRequest req);

    ApiResponse<MerchantDocumentResponse> updateStatus(UpdateMerchantDocumentStatus req);

    ApiResponse<MerchantDocumentResponseDeleteAt> trash(Long documentId);

    ApiResponse<MerchantDocumentResponseDeleteAt> restore(Long documentId);

    ApiResponse<Void> deletePermanent(Long documentId);

    ApiResponse<Void> restoreAll();

    ApiResponse<Void> deleteAllPermanent();
}
