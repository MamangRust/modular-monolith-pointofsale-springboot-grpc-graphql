package com.sanedge.pointofsale.order.domain.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull
    private Integer merchantId;

    @NotNull
    private Integer cashierId;

    @Valid
    @NotNull
    private List<CreateOrderItemRequest> items;

}
