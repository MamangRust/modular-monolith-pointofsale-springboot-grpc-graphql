package com.sanedge.pointofsale.cashier.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashierYearTotalSales {
    private String year;
    private Long totalSales;
}
