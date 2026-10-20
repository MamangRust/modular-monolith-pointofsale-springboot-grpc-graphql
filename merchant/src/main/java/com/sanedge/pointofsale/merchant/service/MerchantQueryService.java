package com.sanedge.pointofsale.merchant.service;

import java.util.List;

import com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchants;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantResponseDeleteAt;

public interface MerchantQueryService {
    ApiResponsePagination<List<MerchantResponse>> findAll(FindAllMerchants req);

    ApiResponsePagination<List<MerchantResponseDeleteAt>> findByActive(FindAllMerchants req);

    ApiResponsePagination<List<MerchantResponseDeleteAt>> findByTrashed(FindAllMerchants req);

    ApiResponse<MerchantResponse> findById(Integer merchantId);

    ApiResponse<List<MerchantResponse>> findByUserId(Integer userId);

}
