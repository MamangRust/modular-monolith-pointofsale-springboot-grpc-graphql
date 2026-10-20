package com.sanedge.pointofsale.transaction.domain.responses;

import com.sanedge.pointofsale.transaction.models.TransactionYearlyAmountFailed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionYearlyAmountFailedResponse {
    private String year;
    private Integer totalFailed;
    private Long totalAmount;

    public static TransactionYearlyAmountFailedResponse from(TransactionYearlyAmountFailed entity) {
        return TransactionYearlyAmountFailedResponse.builder()
                .year(entity.getYear())
                .totalFailed(entity.getTotalFailed())
                .totalAmount(entity.getTotalAmount())
                .build();
    }
}