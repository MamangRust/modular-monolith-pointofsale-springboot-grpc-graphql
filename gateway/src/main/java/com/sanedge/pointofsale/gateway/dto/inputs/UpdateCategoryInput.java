package com.sanedge.pointofsale.gateway.dto.inputs;

public record UpdateCategoryInput(Integer categoryId, String name, String description, String slugCategory) {
}
