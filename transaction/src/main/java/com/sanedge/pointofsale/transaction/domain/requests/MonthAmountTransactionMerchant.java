package com.sanedge.pointofsale.transaction.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthAmountTransactionMerchant {

    @NotNull
    private Integer merchantId;

    @NotNull
    private Integer year;

    @NotNull
    private Integer month;
}
