package com.sanedge.pointofsale.cashier.service.stats;

import java.util.List;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales;

public interface CashierSalesService {
    ApiResponse<List<CashierResponseMonthSales>> findMonthlySales(Integer year);

    ApiResponse<List<CashierResponseYearSales>> findYearlySales(Integer year);
}
