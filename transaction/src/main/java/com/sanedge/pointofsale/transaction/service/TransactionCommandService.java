package com.sanedge.pointofsale.transaction.service;

import com.sanedge.pointofsale.transaction.domain.requests.CreateTransactionRequest;
import com.sanedge.pointofsale.transaction.domain.requests.UpdateTransactionRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionResponse;
import com.sanedge.pointofsale.transaction.domain.responses.TransactionResponseDeleteAt;

public interface TransactionCommandService {
    ApiResponse<TransactionResponse> create(CreateTransactionRequest request);

    ApiResponse<TransactionResponse> update(UpdateTransactionRequest request);

    ApiResponse<TransactionResponseDeleteAt> trash(Integer id);

    ApiResponse<TransactionResponseDeleteAt> restore(Integer id);

    ApiResponse<Boolean> delete(Integer id);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();
}
