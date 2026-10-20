package com.sanedge.pointofsale.product.domain.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProductRequest {

    @NotNull
    private Integer merchantId;

    @NotNull
    private Integer categoryId;

    @NotNull
    @Size(min = 1)
    private String name;

    @NotNull
    private String description;

    @NotNull
    @Min(0)
    private Integer price;

    @NotNull
    @Min(0)
    private Integer countInStock;

    @NotNull
    private String brand;

    @NotNull
    @Min(0)
    private Integer weight;

    @NotNull
    @Min(0)
    private Integer rating;

    @NotNull
    private String slugProduct;

    @NotNull
    private String imageProduct;
}