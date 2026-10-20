package com.sanedge.pointofsale.cashier.domain.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FindAllCashiers {
    @NotBlank(message = "search is required")
    private String search;

    @Min(value = 1, message = "page harus minimal 1")
    private Integer page;

    @Min(value = 1, message = "page_size minimal 1")
    @Max(value = 100, message = "page_size maksimal 100")
    private Integer pageSize;
}
