package com.sanedge.pointofsale.role.domain.requests;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FindAllRoles {
    @Min(value = 1, message = "Page minimal 1")
    private Integer page = 1;

    @Min(value = 1, message = "Page size minimal 1")
    private Integer pageSize = 10;

    private String search = "";
}
