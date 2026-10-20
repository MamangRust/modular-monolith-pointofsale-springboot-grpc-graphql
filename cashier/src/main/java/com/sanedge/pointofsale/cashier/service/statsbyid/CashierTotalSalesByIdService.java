package com.sanedge.pointofsale.cashier.service.statsbyid;

import java.util.List;

import com.sanedge.pointofsale.cashier.domain.requests.MonthTotalSalesCashier;
import com.sanedge.pointofsale.cashier.domain.requests.YearTotalSalesCashier;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthTotalSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearTotalSales;

public interface CashierTotalSalesByIdService {
    ApiResponse<List<CashierResponseMonthTotalSales>> findMonthlyTotalSalesById(MonthTotalSalesCashier req);

    ApiResponse<List<CashierResponseYearTotalSales>> findYearlyTotalSalesById(YearTotalSalesCashier req);
}
