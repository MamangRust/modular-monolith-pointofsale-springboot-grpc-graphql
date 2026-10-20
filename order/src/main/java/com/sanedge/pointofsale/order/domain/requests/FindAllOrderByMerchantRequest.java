package com.sanedge.pointofsale.order.domain.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FindAllOrderByMerchantRequest {

    @NotNull
    private Integer merchantId;

    private String search;

    @Min(1)
    private Integer page = 1;

    @Min(1)
    private Integer pageSize = 10;
}
