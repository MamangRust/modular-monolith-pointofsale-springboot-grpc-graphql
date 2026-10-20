package com.sanedge.pointofsale.category.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthTotalPrice {
    @NotNull
    private Integer year;

    @NotNull
    private Integer month;
}
