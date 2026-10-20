package com.sanedge.pointofsale.cashier.service;

import com.sanedge.pointofsale.cashier.domain.requests.CreateCashierRequest;
import com.sanedge.pointofsale.cashier.domain.requests.UpdateCashierRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseDeleteAt;

public interface CashierCommandService {
    ApiResponse<CashierResponse> createCashier(CreateCashierRequest req);

    ApiResponse<CashierResponse> updateCashier(UpdateCashierRequest req);

    ApiResponse<CashierResponseDeleteAt> trashedCashier(Integer cashierId);

    ApiResponse<CashierResponseDeleteAt> restoreCashier(Integer cashierId);

    ApiResponse<Boolean> deleteCashierPermanent(Integer cashierId);

    ApiResponse<Boolean> restoreAllCashier();

    ApiResponse<Boolean> deleteAllCashierPermanent();
}
