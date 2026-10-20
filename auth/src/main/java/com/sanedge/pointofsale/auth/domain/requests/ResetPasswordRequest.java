package com.sanedge.pointofsale.auth.domain.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Reset-password payload: one-time token plus the new password. */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token wajib diisi")
    private String token;

    @Size(min = 6, message = "Password minimal 6 karakter")
    @NotBlank(message = "Password wajib diisi")
    private String password;

    @Size(min = 6, message = "Confirm password minimal 6 karakter")
    @NotBlank(message = "Confirm password wajib diisi")
    private String confirmPassword;
}
