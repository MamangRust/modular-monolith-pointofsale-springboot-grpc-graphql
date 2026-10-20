package com.sanedge.pointofsale.merchant.domain.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMerchantRequest {
    @NotNull(message = "merchant_id is required")
    private Integer merchantId;

    @NotNull(message = "user_id is required")
    private Integer userId;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "description is required")
    private String description;

    @NotBlank(message = "address is required")
    private String address;

    @NotBlank(message = "contact_email is required")
    @Email(message = "contact_email harus format email valid")
    private String contactEmail;

    @NotBlank(message = "contact_phone is required")
    private String contactPhone;

    @NotBlank(message = "status is required")
    private String status;
}
