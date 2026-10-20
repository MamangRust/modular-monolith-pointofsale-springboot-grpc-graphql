package com.sanedge.pointofsale.gateway.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMonthlyMethodResponse {
    private String month;
    private String paymentMethod;
    private Integer totalTransactions;
    private Long totalAmount;
}
