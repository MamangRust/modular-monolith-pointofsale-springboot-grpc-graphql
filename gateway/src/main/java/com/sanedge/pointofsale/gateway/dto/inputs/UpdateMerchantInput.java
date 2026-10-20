package com.sanedge.pointofsale.gateway.dto.inputs;

public record UpdateMerchantInput(
        Integer merchantId,
        Integer userId,
        String name,
        String description,
        String address,
        String contactEmail,
        String contactPhone,
        String status) {
}
