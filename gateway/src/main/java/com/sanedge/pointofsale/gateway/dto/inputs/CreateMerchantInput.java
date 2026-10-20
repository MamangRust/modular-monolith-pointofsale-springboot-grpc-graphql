package com.sanedge.pointofsale.gateway.dto.inputs;

public record CreateMerchantInput(
        Integer userId,
        String name,
        String description,
        String address,
        String contactEmail,
        String contactPhone,
        String status) {
}
