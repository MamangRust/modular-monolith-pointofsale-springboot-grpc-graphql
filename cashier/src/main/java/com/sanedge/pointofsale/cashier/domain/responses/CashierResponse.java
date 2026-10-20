package com.sanedge.pointofsale.cashier.domain.responses;

import com.sanedge.pointofsale.cashier.models.Cashier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CashierResponse {
    private Integer id;
    private Integer merchantId;
    private String name;
    private String createdAt;
    private String updatedAt;

    public static CashierResponse from(Cashier cashier) {
        return CashierResponse.builder()
                .id(cashier.getCashierId().intValue())
                .merchantId(cashier.getMerchantId().intValue())
                .name(cashier.getName())
                .createdAt(cashier.getCreatedAt().toString())
                .updatedAt(cashier.getUpdatedAt().toString())
                .build();
    }
}