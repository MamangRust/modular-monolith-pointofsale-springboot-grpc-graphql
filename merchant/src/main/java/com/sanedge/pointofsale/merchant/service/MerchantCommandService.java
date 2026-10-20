package com.sanedge.pointofsale.merchant.service;

import com.sanedge.pointofsale.merchant.domain.requests.CreateMerchantRequest;
import com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantResponseDeleteAt;

public interface MerchantCommandService {
    ApiResponse<MerchantResponse> createMerchant(CreateMerchantRequest req);

    ApiResponse<MerchantResponse> updateMerchant(UpdateMerchantRequest req);

    ApiResponse<MerchantResponse> updateMerchantStatus(Integer merchantId, String status);

    ApiResponse<MerchantResponseDeleteAt> trashedMerchant(Integer merchantId);

    ApiResponse<MerchantResponseDeleteAt> restoreMerchant(Integer merchantId);

    ApiResponse<Boolean> deleteMerchantPermanent(Integer merchantId);

    ApiResponse<Boolean> restoreAllMerchant();

    ApiResponse<Boolean> deleteAllMerchantPermanent();
}
