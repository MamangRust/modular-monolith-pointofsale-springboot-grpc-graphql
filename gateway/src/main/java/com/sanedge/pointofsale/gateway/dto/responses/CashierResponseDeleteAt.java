package com.sanedge.pointofsale.gateway.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashierResponseDeleteAt {
    private Integer id;
    private Integer merchantId;
    private String name;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
}
