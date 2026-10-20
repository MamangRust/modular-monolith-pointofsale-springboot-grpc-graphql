package com.sanedge.pointofsale.gateway.dto.inputs;

public record UpdateTransactionInput(
        Integer transactionID,
        Integer orderID,
        Integer merchantID,
        String paymentMethod,
        Integer amount,
        String paymentStatus) {
}
