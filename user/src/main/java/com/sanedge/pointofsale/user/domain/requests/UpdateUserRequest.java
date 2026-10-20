package com.sanedge.pointofsale.user.domain.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @Min(value = 1, message = "ID must be greater than 0")
    private Integer id;

    private String username;

    private String firstname;

    private String lastname;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(min = 6, message = "Confirm password must be at least 6 characters")
    private String confirmPassword;
}