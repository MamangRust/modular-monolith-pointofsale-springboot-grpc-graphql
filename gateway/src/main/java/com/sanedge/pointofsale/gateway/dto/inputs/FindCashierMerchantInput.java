package com.sanedge.pointofsale.gateway.dto.inputs;

public record FindCashierMerchantInput(Integer merchantId, Integer page, Integer pageSize, String search) {
}
