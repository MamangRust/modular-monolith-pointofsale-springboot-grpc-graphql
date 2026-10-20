package com.sanedge.pointofsale.gateway.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDeleteAt {
    private Long id;
    private Integer merchantId;
    private Integer cashierId;
    private Long totalPrice;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
}
