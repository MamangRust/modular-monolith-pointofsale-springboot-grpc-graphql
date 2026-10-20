package com.sanedge.pointofsale.category.repository.statsbyid;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.category.models.Category;
import com.sanedge.pointofsale.category.models.CategoryMonthPrice;
import com.sanedge.pointofsale.category.models.CategoryYearPrice;

@Repository
public interface CategoryPriceByIdRepository extends JpaRepository<Category, Long> {

    @Query(value = """
            WITH date_range AS (
                SELECT
                    date_trunc('month', make_date(:startDate, 1, 1)) AS start_date,
                    date_trunc('month', make_date(:startDate, 1, 1)) + INTERVAL '1' YEAR - INTERVAL '1' DAY AS end_date
            ),
            monthly_category_stats AS (
                SELECT
                    CAST(c.category_id AS INTEGER) AS categoryId,
                    c.name AS categoryName,
                    date_trunc('month', o.created_at) AS activityMonth,
                    CAST(COUNT(DISTINCT o.order_id) AS INTEGER) AS orderCount,
                    CAST(COALESCE(SUM(oi.quantity), 0) AS INTEGER) AS itemsSold,
                    CAST(COALESCE(SUM(o.total_price), 0) AS BIGINT) AS totalRevenue
                FROM
                    orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                JOIN products p ON oi.product_id = p.product_id
                JOIN categories c ON p.category_id = c.category_id
                WHERE
                    c.category_id = :categoryId
                    AND o.deleted_at IS NULL
                    AND oi.deleted_at IS NULL
                    AND p.deleted_at IS NULL
                    AND c.deleted_at IS NULL
                    AND o.created_at BETWEEN (SELECT start_date FROM date_range)
                                         AND (SELECT end_date FROM date_range)
                GROUP BY
                    c.category_id, c.name, activityMonth
            )
            SELECT
                TO_CHAR(mcs.activityMonth, 'Mon') AS month,
                mcs.categoryId,
                mcs.categoryName,
                mcs.orderCount,
                mcs.itemsSold,
                mcs.totalRevenue
            FROM
                monthly_category_stats mcs
            ORDER BY
                mcs.activityMonth, mcs.totalRevenue DESC
            """, nativeQuery = true)
    List<CategoryMonthPrice> findMonthlyCategoryPriceById(
            @Param("categoryId") Long categoryId,
            @Param("startDate") Integer startDate);

    @Query(value = """
            WITH last_five_years AS (
                SELECT
                    CAST(c.category_id AS INTEGER) AS categoryId,
                    c.name AS categoryName,
                    CAST(EXTRACT(YEAR FROM o.created_at) AS VARCHAR) AS year,
                    CAST(COUNT(DISTINCT o.order_id) AS INTEGER) AS orderCount,
                    CAST(COALESCE(SUM(oi.quantity), 0) AS INTEGER) AS itemsSold,
                    CAST(COALESCE(SUM(o.total_price), 0) AS BIGINT) AS totalRevenue,
                    CAST(COUNT(DISTINCT oi.product_id) AS INTEGER) AS uniqueProductsSold
                FROM
                    orders o
                JOIN order_items oi ON o.order_id = oi.order_id
                JOIN products p ON oi.product_id = p.product_id
                JOIN categories c ON p.category_id = c.category_id
                WHERE
                    c.category_id = :categoryId
                    AND o.deleted_at IS NULL
                    AND oi.deleted_at IS NULL
                    AND p.deleted_at IS NULL
                    AND c.deleted_at IS NULL
                    AND EXTRACT(YEAR FROM o.created_at)
                        BETWEEN (:startDate - 4) AND :startDate
                GROUP BY
                    c.category_id, c.name, CAST(EXTRACT(YEAR FROM o.created_at) AS VARCHAR)
            )
            SELECT
                year,
                categoryId,
                categoryName,
                orderCount,
                itemsSold,
                totalRevenue,
                uniqueProductsSold
            FROM
                last_five_years
            ORDER BY
                year, totalRevenue DESC
            """, nativeQuery = true)
    List<CategoryYearPrice> findYearlyCategoryPriceById(
            @Param("categoryId") Long categoryId,
            @Param("startDate") Integer startDate);
}
