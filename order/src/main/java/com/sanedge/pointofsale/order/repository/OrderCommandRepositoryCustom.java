package com.sanedge.pointofsale.order.repository;

import com.sanedge.pointofsale.order.models.Order;

public interface OrderCommandRepositoryCustom {
    Order trashed(Long orderId);

    Order restore(Long orderId);

    Order deletePermanent(Long orderId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}