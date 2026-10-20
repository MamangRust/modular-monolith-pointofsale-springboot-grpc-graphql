package com.sanedge.pointofsale.gateway.dto.inputs;

import java.util.List;

public record CreateOrderInput(Integer merchantId, Integer cashierId, List<CreateOrderItemInput> items) {
}
