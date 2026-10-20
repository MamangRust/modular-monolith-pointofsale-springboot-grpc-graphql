package com.sanedge.pointofsale.gateway.dto.inputs;

public record CreateProductInput(
        Integer merchantId,
        Integer categoryId,
        String name,
        String description,
        Integer price,
        Integer countInStock,
        String brand,
        Integer weight,
        Integer rating,
        String slugProduct,
        String imageProductBase64,
        String imageProductFilename) {
}
