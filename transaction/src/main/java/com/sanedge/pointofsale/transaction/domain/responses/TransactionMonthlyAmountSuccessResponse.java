package com.sanedge.pointofsale.transaction.domain.responses;

import com.sanedge.pointofsale.transaction.models.TransactionMonthlyAmountSuccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMonthlyAmountSuccessResponse {
    private String year;
    private String month;
    private Integer totalSuccess;
    private Long totalAmount;

    public static TransactionMonthlyAmountSuccessResponse from(TransactionMonthlyAmountSuccess entity) {
        return TransactionMonthlyAmountSuccessResponse.builder()
                .year(entity.getYear())
                .month(entity.getMonth())
                .totalSuccess(entity.getTotalSuccess())
                .totalAmount(entity.getTotalAmount())
                .build();
    }
}
