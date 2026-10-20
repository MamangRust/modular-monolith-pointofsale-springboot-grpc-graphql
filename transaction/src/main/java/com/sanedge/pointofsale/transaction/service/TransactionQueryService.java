package com.sanedge.pointofsale.transaction.service;

import java.util.List;

import com.sanedge.pointofsale.transaction.domain.requests.FindAllTransactionByMerchantRequest;
import com.sanedge.pointofsale.transaction.domain.requests.FindAllTransactionRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionResponseDeleteAt;

public interface TransactionQueryService {
    ApiResponsePagination<List<TransactionResponse>> findAllTransactions(FindAllTransactionRequest req);

    ApiResponsePagination<List<TransactionResponseDeleteAt>> findByActive(FindAllTransactionRequest req);

    ApiResponsePagination<List<TransactionResponseDeleteAt>> findByTrashed(FindAllTransactionRequest req);

    ApiResponsePagination<List<TransactionResponse>> findByMerchant(FindAllTransactionByMerchantRequest req);

    ApiResponse<TransactionResponse> findById(Integer id);

    ApiResponse<TransactionResponse> findByOrderId(Integer id);
}
