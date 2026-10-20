package com.sanedge.pointofsale.user.domain.requests;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAllUserRequest {

    @Min(value = 1, message = "Page harus minimal 1")
    private Integer page = 1;

    @Min(value = 1, message = "Page size harus minimal 1")
    private Integer pageSize = 10;

    private String search = "";
}