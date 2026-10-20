package com.sanedge.pointofsale.gateway.dto.inputs;

public record CreateTransactionInput(Integer orderID, Integer merchantID, String paymentMethod, Integer amount) {
}
