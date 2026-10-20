package com.sanedge.pointofsale.cashier.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthTotalSalesMerchant {
    @NotNull
    private Integer merchantId;

    @NotNull
    private Integer year;

    @NotNull
    private Integer month;
}
