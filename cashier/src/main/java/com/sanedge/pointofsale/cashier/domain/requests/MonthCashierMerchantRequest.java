package com.sanedge.pointofsale.cashier.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthCashierMerchantRequest {

    @NotNull(message = "merchant_id is required")
    private Integer merchantId;

    @NotNull(message = "year is required")
    private Integer year;
}