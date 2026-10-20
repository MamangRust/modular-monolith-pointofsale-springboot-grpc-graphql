package com.sanedge.pointofsale.category.repository.statsbymerchant;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.category.models.Category;
import com.sanedge.pointofsale.category.models.CategoryMonthTotalPrice;
import com.sanedge.pointofsale.category.models.CategoryYearTotalPrice;

@Repository
public interface CategoryTotalPriceByMerchantRepository extends JpaRepository<Category, Long> {
    @Query(value = """
            WITH date_range AS (
                SELECT
                    make_date(:startYear, :startMonth, 1) AS start_date,
                    (make_date(:endYear, :endMonth, 1) + INTERVAL '1' MONTH - INTERVAL '1' DAY) AS end_date
            ),
            monthly_totals AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM o.created_at) AS VARCHAR) AS year,
                    CAST(EXTRACT(MONTH FROM o.created_at) AS INTEGER) AS month,
                    CAST(COALESCE(SUM(o.total_price), 0) AS BIGINT) AS totalRevenue
                FROM orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                JOIN products p ON oi.product_id = p.product_id
                JOIN categories c ON p.category_id = c.category_id
                WHERE o.deleted_at IS NULL
                  AND oi.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                  AND c.deleted_at IS NULL
                  AND o.merchant_id = :merchantId
                  AND o.created_at BETWEEN (SELECT start_date FROM date_range) AND (SELECT end_date FROM date_range)
                GROUP BY CAST(EXTRACT(YEAR FROM o.created_at) AS VARCHAR), CAST(EXTRACT(MONTH FROM o.created_at) AS INTEGER)
            ),
            all_months AS (
                SELECT CAST(:startYear AS VARCHAR) AS year, CAST(:startMonth AS INTEGER) AS month, TO_CHAR(make_date(:startYear, :startMonth, 1), 'FMMonth') AS month_name
                UNION
                SELECT CAST(:endYear AS VARCHAR) AS year, CAST(:endMonth AS INTEGER) AS month, TO_CHAR(make_date(:endYear, :endMonth, 1), 'FMMonth') AS month_name
            )
            SELECT
                am.year,
                am.month_name AS month,
                CAST(COALESCE(mt.totalRevenue, 0) AS BIGINT) AS totalRevenue
            FROM all_months am
            LEFT JOIN monthly_totals mt ON am.year = mt.year AND am.month = mt.month
            ORDER BY CAST(am.year AS INTEGER) DESC, am.month DESC
            """, nativeQuery = true)
    List<CategoryMonthTotalPrice> findMonthlyTotalPriceByMerchant(
            @Param("merchantId") Long merchantId,
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM o.created_at) AS INTEGER) AS year,
                    CAST(COALESCE(SUM(o.total_price), 0) AS BIGINT) AS totalRevenue
                FROM orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                JOIN products p ON oi.product_id = p.product_id
                JOIN categories c ON p.category_id = c.category_id
                WHERE o.deleted_at IS NULL
                  AND oi.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                  AND c.deleted_at IS NULL
                  AND o.merchant_id = :merchantId
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
                CAST(COALESCE(yd.totalRevenue, 0) AS BIGINT) AS totalRevenue
            FROM all_years a
            LEFT JOIN yearly_data yd ON a.year = yd.year
            ORDER BY a.year DESC
            """, nativeQuery = true)
    List<CategoryYearTotalPrice> findYearlyTotalPriceByMerchant(
            @Param("merchantId") Long merchantId,
            @Param("year") Integer year,
            @Param("yearMinusOne") Integer yearMinusOne);
}
