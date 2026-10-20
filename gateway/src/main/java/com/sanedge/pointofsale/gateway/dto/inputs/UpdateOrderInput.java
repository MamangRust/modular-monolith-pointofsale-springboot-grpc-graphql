package com.sanedge.pointofsale.gateway.dto.inputs;

import java.util.List;

public record UpdateOrderInput(Integer orderId, Integer cashierId, List<UpdateOrderItemInput> items) {
}
