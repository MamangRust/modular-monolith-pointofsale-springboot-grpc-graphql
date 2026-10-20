package com.sanedge.pointofsale.product.domain.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FindAllProductByCategoryRequest {

    @NotNull
    private String categoryName;

    private String search;

    @Min(0)
    private Integer minPrice;

    @Min(0)
    private Integer maxPrice;

    @NotNull
    @Min(1)
    private Integer page;

    @NotNull
    @Min(1)
    private Integer pageSize;
}