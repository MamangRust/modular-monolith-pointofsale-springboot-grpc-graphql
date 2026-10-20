package com.sanedge.pointofsale.product.domain.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FindAllProductRequest {

    @NotBlank
    private String search;

    @NotNull
    @Min(1)
    private Integer page;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer pageSize;
}