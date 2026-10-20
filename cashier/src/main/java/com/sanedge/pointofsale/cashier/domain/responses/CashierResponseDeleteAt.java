package com.sanedge.pointofsale.cashier.domain.responses;

import com.sanedge.pointofsale.cashier.models.Cashier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CashierResponseDeleteAt {
    private Integer id;
    private Integer merchantId;
    private String name;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public static CashierResponseDeleteAt from(Cashier cashier) {
        return CashierResponseDeleteAt.builder()
                .id(cashier.getCashierId().intValue())
                .merchantId(cashier.getMerchantId().intValue())
                .name(cashier.getName())
                .createdAt(cashier.getCreatedAt().toString())
                .updatedAt(cashier.getUpdatedAt().toString())
                .deletedAt(cashier.getDeletedAt() != null ? cashier.getDeletedAt().toString() : null)
                .build();
    }
}
