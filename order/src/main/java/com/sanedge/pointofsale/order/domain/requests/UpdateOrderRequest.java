package com.sanedge.pointofsale.order.domain.requests;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderRequest {
    @NotNull
    private Integer orderId;

    @NotNull
    private Integer cashierId;

    @Valid
    @NotNull
    private List<UpdateOrderItemRequest> items;
}
