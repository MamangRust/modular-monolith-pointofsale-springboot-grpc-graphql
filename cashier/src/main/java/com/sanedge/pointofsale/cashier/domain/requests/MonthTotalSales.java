package com.sanedge.pointofsale.cashier.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthTotalSales {
    @NotNull
    private Integer year;

    @NotNull
    private Integer month;
}
