package com.sanedge.pointofsale.order.repository;

import com.sanedge.pointofsale.order.models.Order;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderQueryRepository extends JpaRepository<Order, Long> {
    @Query("""
            SELECT o FROM Order o
            WHERE (:keyword IS NULL
                OR CAST(o.orderId AS string) LIKE CONCAT('%', :keyword, '%')
                OR CAST(o.cashierId AS string) LIKE CONCAT('%', :keyword, '%'))
            ORDER BY o.createdAt ASC
            """)
    Page<Order> findOrders(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT o FROM Order o
            WHERE o.merchantId = :merchantId
            AND (:keyword IS NULL
                OR CAST(o.orderId AS string) LIKE CONCAT('%', :keyword, '%')
                OR CAST(o.cashierId AS string) LIKE CONCAT('%', :keyword, '%'))
            ORDER BY o.createdAt ASC
            """)
    Page<Order> findOrdersByMerchant(@Param("merchantId") Long merchantId,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("""
            SELECT o FROM Order o
            WHERE o.deletedAt IS NULL
            AND (:keyword IS NULL
                OR CAST(o.orderId AS string) LIKE CONCAT('%', :keyword, '%')
                OR CAST(o.cashierId AS string) LIKE CONCAT('%', :keyword, '%'))
            ORDER BY o.createdAt ASC
            """)
    Page<Order> findActiveOrders(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
                SELECT o FROM Order o
                WHERE o.orderId = :orderId
                  AND o.deletedAt IS NULL
            """)
    Optional<Order> findOrderById(@Param("orderId") Long orderId);

    @Query("""
            SELECT o FROM Order o
            WHERE o.deletedAt IS NOT NULL
            AND (:keyword IS NULL
                OR CAST(o.orderId AS string) LIKE CONCAT('%', :keyword, '%')
                OR CAST(o.cashierId AS string) LIKE CONCAT('%', :keyword, '%'))
            ORDER BY o.deletedAt DESC
            """)
    Page<Order> findTrashedOrders(@Param("keyword") String keyword, Pageable pageable);
}
