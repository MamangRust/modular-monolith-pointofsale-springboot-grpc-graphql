package com.sanedge.pointofsale.transaction.service.stats;

import java.util.List;

import com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyMethodResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyMethodResponse;

public interface TransactionMethodService {
    ApiResponse<List<TransactionMonthlyMethodResponse>> findMonthlyMethodSuccess(MonthMethodTransactionRequest req);

    ApiResponse<List<TransactionYearlyMethodResponse>> findYearlyMethodSuccess(Integer year);

    ApiResponse<List<TransactionMonthlyMethodResponse>> findMonthlyMethodFailed(MonthMethodTransactionRequest req);

    ApiResponse<List<TransactionYearlyMethodResponse>> findYearlyMethodFailed(Integer year);
}