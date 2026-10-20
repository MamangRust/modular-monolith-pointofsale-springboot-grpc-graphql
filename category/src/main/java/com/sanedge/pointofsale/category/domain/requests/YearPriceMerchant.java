package com.sanedge.pointofsale.category.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class YearPriceMerchant {
    @NotNull
    private Integer merchantId;

    @NotNull
    private Integer year;
}
