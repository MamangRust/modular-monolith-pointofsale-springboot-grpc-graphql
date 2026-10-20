package com.sanedge.pointofsale.cashier.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthCashierIdRequest {

    @NotNull(message = "cashier_id is required")
    private Integer cashierId;

    @NotNull(message = "year is required")
    private Integer year;
}