package com.sanedge.pointofsale.gateway.dto.inputs;

public record ProductMerchantFilterInput(
        Integer merchantId,
        String search,
        Integer categoryId,
        Integer minPrice,
        Integer maxPrice,
        Integer page,
        Integer pageSize) {
}
