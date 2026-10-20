package com.sanedge.pointofsale.category.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonthTotalPriceCategory {
    @NotNull
    private Integer categoryId;

    @NotNull
    private Integer year;

    @NotNull
    private Integer month;
}
