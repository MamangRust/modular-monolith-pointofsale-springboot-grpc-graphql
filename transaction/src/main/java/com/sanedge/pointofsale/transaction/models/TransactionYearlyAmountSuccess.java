package com.sanedge.pointofsale.transaction.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionYearlyAmountSuccess {
    private String year;
    private Integer totalSuccess;
    private Long totalAmount;
}