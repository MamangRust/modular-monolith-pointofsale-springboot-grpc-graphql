package com.sanedge.pointofsale.transaction.service.stats;

import java.util.List;

import com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountFailedResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountSuccessResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountFailedResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountSuccessResponse;

public interface TransactionAmountService {
    ApiResponse<List<TransactionMonthlyAmountSuccessResponse>> findMonthlyAmountSuccess(
            MonthAmountTransactionRequest req);

    ApiResponse<List<TransactionYearlyAmountSuccessResponse>> findYearlyAmountSuccess(Integer year);

    ApiResponse<List<TransactionMonthlyAmountFailedResponse>> findMonthlyAmountFailed(
            MonthAmountTransactionRequest req);

    ApiResponse<List<TransactionYearlyAmountFailedResponse>> findYearlyAmountFailed(Integer year);
}