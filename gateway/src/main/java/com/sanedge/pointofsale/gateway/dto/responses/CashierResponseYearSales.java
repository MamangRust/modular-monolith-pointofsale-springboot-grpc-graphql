package com.sanedge.pointofsale.gateway.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashierResponseYearSales {
    private String year;
    private Integer cashierId;
    private String cashierName;
    private Integer orderCount;
    private Long totalSales;
}
