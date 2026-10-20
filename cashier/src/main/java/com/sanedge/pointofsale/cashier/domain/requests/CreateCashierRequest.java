package com.sanedge.pointofsale.cashier.domain.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCashierRequest {
    @NotNull
    private Integer merchantId;

    @NotNull
    private Integer userId;

    @NotBlank
    private String name;
}
