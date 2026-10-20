package com.sanedge.pointofsale.category.domain.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    @NotNull
    private Integer categoryId;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private String slugCategory;
}
