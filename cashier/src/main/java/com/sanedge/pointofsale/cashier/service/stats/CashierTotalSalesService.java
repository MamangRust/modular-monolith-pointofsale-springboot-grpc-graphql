package com.sanedge.pointofsale.cashier.service.stats;

import java.util.List;

import com.sanedge.pointofsale.cashier.domain.requests.MonthTotalSales;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthTotalSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearTotalSales;

public interface CashierTotalSalesService {
    ApiResponse<List<CashierResponseMonthTotalSales>> findMonthlyTotalSales(MonthTotalSales req);

    ApiResponse<List<CashierResponseYearTotalSales>> findYearlyTotalSales(Integer year);
}
