package com.sanedge.pointofsale.cashier.service.statsbymerchant;

import java.util.List;

import com.sanedge.pointofsale.cashier.domain.requests.MonthTotalSalesMerchant;
import com.sanedge.pointofsale.cashier.domain.requests.YearTotalSalesMerchant;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthTotalSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearTotalSales;

public interface CashierTotalSalesByMerchantService {
    ApiResponse<List<CashierResponseMonthTotalSales>> findMonthlyTotalSalesByMerchant(MonthTotalSalesMerchant req);

    ApiResponse<List<CashierResponseYearTotalSales>> findYearlyTotalSalesByMerchant(YearTotalSalesMerchant req);
}
