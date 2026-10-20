package com.sanedge.pointofsale.gateway.dto.inputs;

public record UpdateOrderItemInput(Integer orderItemId, Integer productId, Integer quantity, Integer price) {
}
