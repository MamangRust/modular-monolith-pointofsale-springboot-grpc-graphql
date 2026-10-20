package com.sanedge.pointofsale.cashier.repository.statsbyid;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.cashier.models.Cashier;
import com.sanedge.pointofsale.cashier.models.CashierMonthTotalSales;
import com.sanedge.pointofsale.cashier.models.CashierYearTotalSales;

@Repository
public interface CashierTotalSalesByIdRepository extends JpaRepository<Cashier, Long> {
    @Query(value = """
            WITH monthly_totals AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM o.created_at) AS VARCHAR) AS year,
                    CAST(EXTRACT(MONTH FROM o.created_at) AS INTEGER) AS month,
                    CAST(COALESCE(SUM(o.total_price), 0) AS BIGINT) AS totalSales
                FROM orders o
                JOIN cashiers c ON o.cashier_id = c.cashier_id
                WHERE o.deleted_at IS NULL
                  AND c.deleted_at IS NULL
                  AND c.cashier_id = :cashierId
                  AND (
                      (EXTRACT(YEAR FROM o.created_at) = :startYear AND EXTRACT(MONTH FROM o.created_at) = :startMonth)
                      OR (EXTRACT(YEAR FROM o.created_at) = :endYear AND EXTRACT(MONTH FROM o.created_at) = :endMonth)
                  )
                GROUP BY CAST(EXTRACT(YEAR FROM o.created_at) AS VARCHAR), CAST(EXTRACT(MONTH FROM o.created_at) AS INTEGER)
            ),
            all_months AS (
                SELECT CAST(:startYear AS VARCHAR) AS year, CAST(:startMonth AS INTEGER) AS month
                UNION
                SELECT CAST(:endYear AS VARCHAR) AS year, CAST(:endMonth AS INTEGER) AS month
            )
            SELECT
                am.year,
                TO_CHAR(TO_DATE(CAST(am.month AS VARCHAR), 'MM'), 'FMMonth') AS month,
                CAST(COALESCE(mt.totalSales, 0) AS BIGINT) AS totalSales
            FROM all_months am
            LEFT JOIN monthly_totals mt ON am.year = mt.year AND am.month = mt.month
            ORDER BY CAST(am.year AS INTEGER) DESC, am.month DESC
            """, nativeQuery = true)
    List<CashierMonthTotalSales> findMonthTotalSalesById(
            @Param("cashierId") Long cashierId,
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM o.created_at) AS INTEGER) AS year,
                    CAST(COALESCE(SUM(o.total_price), 0) AS BIGINT) AS totalSales
                FROM orders o
                JOIN cashiers c ON o.cashier_id = c.cashier_id
                WHERE o.deleted_at IS NULL
                  AND c.deleted_at IS NULL
                  AND c.cashier_id = :cashierId
                  AND EXTRACT(YEAR FROM o.created_at) IN (:year, :yearMinusOne)
                GROUP BY CAST(EXTRACT(YEAR FROM o.created_at) AS INTEGER)
            ),
            all_years AS (
                SELECT CAST(:year AS INTEGER) AS year
                UNION
                SELECT CAST(:yearMinusOne AS INTEGER) AS year
            )
            SELECT
                CAST(a.year AS VARCHAR) AS year,
                CAST(COALESCE(yd.totalSales, 0) AS BIGINT) AS totalSales
            FROM all_years a
            LEFT JOIN yearly_data yd ON a.year = yd.year
            ORDER BY a.year DESC
            """, nativeQuery = true)
    List<CashierYearTotalSales> findYearTotalSalesById(
            @Param("cashierId") Long cashierId,
            @Param("year") Integer year,
            @Param("yearMinusOne") Integer yearMinusOne);
}
