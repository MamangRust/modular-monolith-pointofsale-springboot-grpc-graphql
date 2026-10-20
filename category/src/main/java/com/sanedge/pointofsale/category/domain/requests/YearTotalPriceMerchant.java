package com.sanedge.pointofsale.category.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class YearTotalPriceMerchant {
    @NotNull
    private Integer merchantId;

    @NotNull
    private Integer year;
}
