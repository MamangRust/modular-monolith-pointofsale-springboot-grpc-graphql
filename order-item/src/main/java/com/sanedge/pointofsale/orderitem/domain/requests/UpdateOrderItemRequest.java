package com.sanedge.pointofsale.orderitem.domain.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderItemRequest {
    @NotNull
    private Integer orderItemId;

    @NotNull
    private Integer orderId;

    @NotNull
    private Integer productId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private Integer price;
}
