package com.sanedge.pointofsale.order.repository.statsbymerchant;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.order.models.Order;
import com.sanedge.pointofsale.order.models.OrderMonth;
import com.sanedge.pointofsale.order.models.OrderYear;

@Repository
public interface OrderSoldOutByMerchantRepository extends JpaRepository<Order, Long> {
    @Query(value = """
            WITH date_range AS (
                SELECT
                    date_trunc('month', TO_TIMESTAMP(:yearMonth, 'YYYYMM')) AS start_date,
                    date_trunc('month', TO_TIMESTAMP(:yearMonth, 'YYYYMM')) + INTERVAL '1' YEAR - INTERVAL '1' DAY AS end_date
            ),
            monthly_orders AS (
                SELECT
                    date_trunc('month', o.created_at) AS activity_month,
                    CAST(COUNT(o.order_id) AS INTEGER) AS order_count,
                    CAST(SUM(o.total_price) AS BIGINT) AS total_revenue,
                    CAST(SUM(oi.quantity) AS INTEGER) AS total_items_sold
                FROM orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                WHERE o.deleted_at IS NULL
                  AND oi.deleted_at IS NULL
                  AND o.merchant_id = :merchantId
                  AND o.created_at BETWEEN (SELECT start_date FROM date_range)
                                       AND (SELECT end_date FROM date_range)
                GROUP BY activity_month
            )
            SELECT
                TO_CHAR(mo.activity_month, 'Mon') AS month,
                mo.order_count AS orderCount,
                mo.total_revenue AS totalRevenue,
                mo.total_items_sold AS totalItemsSold
            FROM monthly_orders mo
            ORDER BY mo.activity_month
            """, nativeQuery = true)
    List<OrderMonth> findMonthlyOrdersByMerchant(
            @Param("merchantId") Integer merchantId,
            @Param("yearMonth") Integer yearMonth);

    @Query(value = """
            WITH last_five_years AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM o.created_at) AS VARCHAR) AS year,
                    CAST(COUNT(o.order_id) AS INTEGER) AS order_count,
                    CAST(SUM(o.total_price) AS BIGINT) AS total_revenue,
                    CAST(SUM(oi.quantity) AS INTEGER) AS total_items_sold,
                    CAST(COUNT(DISTINCT o.user_id) AS INTEGER) AS active_cashiers,
                    CAST(COUNT(DISTINCT oi.product_id) AS INTEGER) AS unique_products_sold
                FROM orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                WHERE o.deleted_at IS NULL
                  AND oi.deleted_at IS NULL
                  AND o.merchant_id = :merchantId
                  AND EXTRACT(YEAR FROM o.created_at) BETWEEN EXTRACT(YEAR FROM TO_TIMESTAMP(:yearMonth, 'YYYYMM')) - 4
                                                           AND EXTRACT(YEAR FROM TO_TIMESTAMP(:yearMonth, 'YYYYMM'))
                GROUP BY CAST(EXTRACT(YEAR FROM o.created_at) AS VARCHAR)
            )
            SELECT
                year,
                order_count AS orderCount,
                total_revenue AS totalRevenue,
                total_items_sold AS totalItemsSold,
                active_cashiers AS activeCashiers,
                unique_products_sold AS uniqueProductsSold
            FROM last_five_years
            ORDER BY year
            """, nativeQuery = true)
    List<OrderYear> findYearlyOrdersByMerchant(
            @Param("merchantId") Integer merchantId,
            @Param("yearMonth") Integer yearMonth);
}
