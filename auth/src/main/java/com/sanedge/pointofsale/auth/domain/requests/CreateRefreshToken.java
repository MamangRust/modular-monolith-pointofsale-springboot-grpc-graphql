package com.sanedge.pointofsale.auth.domain.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRefreshToken {
    @Min(value = 1, message = "User ID wajib >= 1")
    private Integer userId;

    @NotBlank(message = "Token wajib diisi")
    private String token;

    @NotBlank(message = "ExpiresAt wajib diisi")
    private String expiresAt;
}