package com.sanedge.pointofsale.order.domain.requests;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FindAllOrderRequest {

    private String search;

    @Min(1)
    private Integer page = 1;

    @Min(1)
    private Integer pageSize = 10;
}
