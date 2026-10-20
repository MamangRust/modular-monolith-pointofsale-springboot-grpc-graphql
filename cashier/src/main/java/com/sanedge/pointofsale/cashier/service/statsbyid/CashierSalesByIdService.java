package com.sanedge.pointofsale.cashier.service.statsbyid;

import java.util.List;

import com.sanedge.pointofsale.cashier.domain.requests.MonthCashierIdRequest;
import com.sanedge.pointofsale.cashier.domain.requests.YearCashierIdRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseMonthSales;
import com.sanedge.pointofsale.cashier.domain.responses.CashierResponseYearSales;

public interface CashierSalesByIdService {
    ApiResponse<List<CashierResponseMonthSales>> findMonthlyCashierById(MonthCashierIdRequest req);

    ApiResponse<List<CashierResponseYearSales>> findYearlyCashierById(YearCashierIdRequest req);
}
