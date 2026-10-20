package com.sanedge.pointofsale.transaction.service.statsbymerchant;

import java.util.List;

import com.sanedge.pointofsale.transaction.domain.requests.MonthMethodTransactionMerchantRequest;
import com.sanedge.pointofsale.transaction.domain.requests.YearMethodTransactionMerchantRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionMonthlyMethodResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionYearlyMethodResponse;

public interface TransactionMethodByMerchantService {
    ApiResponse<List<TransactionMonthlyMethodResponse>> findMonthlyMethodByMerchantSuccess(
            MonthMethodTransactionMerchantRequest req);

    ApiResponse<List<TransactionMonthlyMethodResponse>> findMonthlyMethodByMerchantFailed(
            MonthMethodTransactionMerchantRequest req);

    ApiResponse<List<TransactionYearlyMethodResponse>> findYearlyMethodByMerchantSuccess(
            YearMethodTransactionMerchantRequest req);

    ApiResponse<List<TransactionYearlyMethodResponse>> findYearlyMethodByMerchantFailed(
            YearMethodTransactionMerchantRequest req);
}