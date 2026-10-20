package com.sanedge.pointofsale.category.domain.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class YearPriceId {
    @NotNull
    private Integer categoryId;

    @NotNull
    private Integer year;
}
