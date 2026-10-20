package com.sanedge.pointofsale.role.domain.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoleRequest {

    @NotBlank(message = "Nama role wajib diisi")
    private String name;
}
