package com.sanedge.pointofsale.cashier.service.statsbymerchant;

import java.util.List;

import com.sanedge.pointofsale.cashier.domain.requests.MonthCashierMerchantRequest;
import com.sanedge.pointofsale.cashier.domain.requests.YearCashierMerchantRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales;

public interface CashierSalesByMerchantService {
    ApiResponse<List<CashierResponseMonthSales>> findMonthlyCashierByMerchant(MonthCashierMerchantRequest req);

    ApiResponse<List<CashierResponseYearSales>> findYearlyCashierByMerchant(YearCashierMerchantRequest req);
}
