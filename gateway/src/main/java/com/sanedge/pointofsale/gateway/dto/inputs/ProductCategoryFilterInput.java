package com.sanedge.pointofsale.gateway.dto.inputs;

public record ProductCategoryFilterInput(
        String categoryName,
        String search,
        Integer minPrice,
        Integer maxPrice,
        Integer page,
        Integer pageSize) {
}
