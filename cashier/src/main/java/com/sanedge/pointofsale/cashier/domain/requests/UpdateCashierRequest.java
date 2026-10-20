package com.sanedge.pointofsale.cashier.domain.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCashierRequest {
    @NotNull
    private Integer cashierId;

    @NotBlank
    private String name;
}
