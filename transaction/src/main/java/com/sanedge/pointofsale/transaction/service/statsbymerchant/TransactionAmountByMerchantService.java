package com.sanedge.pointofsale.transaction.service.statsbymerchant;

import java.util.List;

import com.sanedge.pointofsale.transaction.domain.requests.MonthAmountTransactionMerchant;
import com.sanedge.pointofsale.transaction.domain.requests.YearAmountTransactionMerchant;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountFailedResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyAmountSuccessResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountFailedResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyAmountSuccessResponse;

public interface TransactionAmountByMerchantService {
    ApiResponse<List<TransactionMonthlyAmountSuccessResponse>> findMonthlyAmountSuccessByMerchant(
            MonthAmountTransactionMerchant req);

    ApiResponse<List<TransactionYearlyAmountSuccessResponse>> findYearlyAmountSuccessByMerchant(
            YearAmountTransactionMerchant req);

    ApiResponse<List<TransactionMonthlyAmountFailedResponse>> findMonthlyAmountFailedByMerchant(
            MonthAmountTransactionMerchant req);

    ApiResponse<List<TransactionYearlyAmountFailedResponse>> findYearlyAmountFailedByMerchant(
            YearAmountTransactionMerchant req);
}