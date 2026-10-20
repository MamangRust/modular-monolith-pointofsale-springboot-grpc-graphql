package com.sanedge.pointofsale.gateway.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponse {
    private Long id;
    private Integer userId;
    private String name;
    private String description;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private String status;
    private String createdAt;
    private String updatedAt;
}
