package com.sanedge.pointofsale.category.domain.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private String slugCategory;
}
