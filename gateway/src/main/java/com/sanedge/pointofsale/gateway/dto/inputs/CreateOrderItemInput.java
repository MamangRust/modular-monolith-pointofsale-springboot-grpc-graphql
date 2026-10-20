package com.sanedge.pointofsale.gateway.dto.inputs;

public record CreateOrderItemInput(Integer productId, Integer quantity, Integer price) {
}
