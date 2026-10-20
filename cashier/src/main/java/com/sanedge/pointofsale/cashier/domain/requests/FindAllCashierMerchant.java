package com.sanedge.pointofsale.cashier.domain.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FindAllCashierMerchant {
    @NotNull(message = "merchant_id is required")
    private Integer merchantId;

    @NotBlank(message = "search is required")
    private String search;

    @Min(1)
    private Integer page;

    @Min(1)
    @Max(100)
    private Integer pageSize;
}
