package com.sanedge.pointofsale.orderitem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.orderitem.models.OrderItem;

@Repository
public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    @Query(value = """
            SELECT * FROM order_items
            WHERE order_id = :orderId
              AND deleted_at IS NULL
            """, nativeQuery = true)
    List<OrderItem> findOrderItemByOrder(@Param("orderId") Long orderId);
}
