package com.sanedge.pointofsale.cashier.domain.responses;

import com.sanedge.pointofsale.cashier.models.CashierYearSales;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CashierResponseYearSales {
    private String year;
    private Integer cashierId;
    private String cashierName;
    private Integer orderCount;
    private Long totalSales;

    public static CashierResponseYearSales from(CashierYearSales entity) {
        return CashierResponseYearSales.builder()
                .year(entity.getYear())
                .cashierId(entity.getCashierId())
                .cashierName(entity.getCashierName())
                .orderCount(entity.getOrderCount())
                .totalSales(entity.getTotalSales())
                .build();
    }
}
