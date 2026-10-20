package com.sanedge.pointofsale.order.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthTotalRevenue {
    @NotNull
    private Integer year;

    @NotNull
    private Integer month;
}
