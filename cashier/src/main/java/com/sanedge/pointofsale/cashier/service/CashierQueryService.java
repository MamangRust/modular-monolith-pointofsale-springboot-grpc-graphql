package com.sanedge.pointofsale.cashier.service;

import java.util.List;

import com.sanedge.pointofsale.cashier.domain.requests.FindAllCashierMerchant;
import com.sanedge.pointofsale.cashier.domain.requests.FindAllCashiers;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt;

public interface CashierQueryService {
    ApiResponsePagination<List<CashierResponse>> findAll(FindAllCashiers req);

    ApiResponse<CashierResponse> findById(Integer cashierId);

    ApiResponsePagination<List<CashierResponseDeleteAt>> findByActive(FindAllCashiers req);

    ApiResponsePagination<List<CashierResponseDeleteAt>> findByTrashed(FindAllCashiers req);

    ApiResponsePagination<List<CashierResponse>> findByMerchant(FindAllCashierMerchant req);
}
