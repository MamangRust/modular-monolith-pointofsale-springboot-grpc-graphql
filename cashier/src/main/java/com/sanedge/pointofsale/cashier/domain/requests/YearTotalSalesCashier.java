package com.sanedge.pointofsale.cashier.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class YearTotalSalesCashier {
    @NotNull
    private Integer cashierId;

    @NotNull
    private Integer year;
}
