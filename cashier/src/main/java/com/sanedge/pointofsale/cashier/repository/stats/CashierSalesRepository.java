package com.sanedge.pointofsale.cashier.repository.stats;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.cashier.models.Cashier;
import com.sanedge.pointofsale.cashier.models.CashierMonthSales;
import com.sanedge.pointofsale.cashier.models.CashierYearSales;

@Repository
public interface CashierSalesRepository extends JpaRepository<Cashier, Long> {

    @Query(value = """
            WITH date_range AS (
                SELECT
                    make_date(:year, :startMonth, 1) AS start_date,
                    make_date(:year, :endMonth, 1)
                        + INTERVAL '1' MONTH * (1 + :endMonth - :startMonth)
                        - INTERVAL '1' DAY AS end_date
            ),
            cashier_activity AS (
                SELECT
                    c.cashier_id AS cashierId,
                    c.name AS cashierName,
                    EXTRACT(MONTH FROM o.created_at) AS activityMonth,
                    COUNT(o.order_id) AS orderCount,
                    CAST(COALESCE(SUM(o.total_price), 0) AS BIGINT) AS totalSales
                FROM orders o
                JOIN cashiers c ON o.cashier_id = c.cashier_id
                WHERE o.deleted_at IS NULL
                  AND c.deleted_at IS NULL
                  AND o.created_at BETWEEN
                        (SELECT start_date FROM date_range)
                        AND (SELECT end_date FROM date_range)
                GROUP BY c.cashier_id, c.name, EXTRACT(MONTH FROM o.created_at)
            )
            SELECT
                ca.cashierId,
                ca.cashierName,
                TO_CHAR(TO_DATE(CAST(ca.activityMonth AS VARCHAR), 'MM'), 'Mon') AS month,
                ca.orderCount,
                ca.totalSales
            FROM cashier_activity ca
            ORDER BY ca.activityMonth, ca.cashierId
            """, nativeQuery = true)
    List<CashierMonthSales> findMonthSales(
            @Param("year") Integer year,
            @Param("startMonth") Integer startMonth,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            WITH last_five_years AS (
                SELECT
                    c.cashier_id AS cashierId,
                    c.name AS cashierName,
                    CAST(EXTRACT(YEAR FROM o.created_at) AS VARCHAR) AS year,
                    COUNT(o.order_id) AS orderCount,
                    CAST(COALESCE(SUM(o.total_price), 0) AS BIGINT) AS totalSales
                FROM orders o
                JOIN cashiers c ON o.cashier_id = c.cashier_id
                WHERE o.deleted_at IS NULL
                  AND c.deleted_at IS NULL
                  AND EXTRACT(YEAR FROM o.created_at)
                        BETWEEN (:year - 4) AND :year
                GROUP BY c.cashier_id, c.name, EXTRACT(YEAR FROM o.created_at)
            )
            SELECT
                year,
                cashierId,
                cashierName,
                orderCount,
                totalSales
            FROM last_five_years
            ORDER BY year, cashierId
            """, nativeQuery = true)
    List<CashierYearSales> findYearSales(@Param("year") Integer year);
}
