package com.sanedge.pointofsale.transaction.repository.stats;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.transaction.models.Transaction;
import com.sanedge.pointofsale.transaction.models.TransactionMonthlyAmountFailed;
import com.sanedge.pointofsale.transaction.models.TransactionMonthlyAmountSuccess;
import com.sanedge.pointofsale.transaction.models.TransactionYearlyAmountFailed;
import com.sanedge.pointofsale.transaction.models.TransactionYearlyAmountSuccess;

@Repository
public interface TransactionAmountStatusRepository extends JpaRepository<Transaction, Long> {
    @Query(value = """
            WITH monthly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.created_at) AS INTEGER) AS year,
                    CAST(EXTRACT(MONTH FROM t.created_at) AS INTEGER) AS month,
                    COUNT(*) AS total_success,
                    CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS total_amount
                FROM transactions t
                WHERE
                    t.deleted_at IS NULL
                    AND t.status = 'SUCCESS'
                    AND (
                        (t.created_at >= make_date(:year, :month, 1)
                         AND t.created_at < (make_date(:year, :month, 1) + INTERVAL '1' MONTH))
                        OR
                        (t.created_at >= make_date(:prevYear, :prevMonth, 1)
                         AND t.created_at < (make_date(:prevYear, :prevMonth, 1) + INTERVAL '1' MONTH))
                    )
                GROUP BY CAST(EXTRACT(YEAR FROM t.created_at) AS INTEGER), CAST(EXTRACT(MONTH FROM t.created_at) AS INTEGER)
            ),
            formatted_data AS (
                SELECT
                    CAST(year AS VARCHAR) AS year,
                    TO_CHAR(TO_DATE(CAST(month AS VARCHAR), 'MM'), 'Mon') AS month,
                    CAST(total_success AS INTEGER) AS totalSuccess,
                    CAST(total_amount AS BIGINT) AS totalAmount
                FROM monthly_data
                UNION ALL
                SELECT CAST(:year AS VARCHAR),
                       TO_CHAR(make_date(:year, :month, 1), 'Mon'),
                       0, 0
                WHERE NOT EXISTS (
                    SELECT 1 FROM monthly_data
                    WHERE year = :year AND month = :month
                )
                UNION ALL
                SELECT CAST(:prevYear AS VARCHAR),
                       TO_CHAR(make_date(:prevYear, :prevMonth, 1), 'Mon'),
                       0, 0
                WHERE NOT EXISTS (
                    SELECT 1 FROM monthly_data
                    WHERE year = :prevYear AND month = :prevMonth
                )
            )
            SELECT * FROM formatted_data
            ORDER BY year DESC, TO_DATE(month, 'Mon') DESC
            """, nativeQuery = true)
    List<TransactionMonthlyAmountSuccess> findMonthlyTransactionSuccess(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("prevYear") Integer prevYear,
            @Param("prevMonth") Integer prevMonth);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.created_at) AS INTEGER) AS year,
                    COUNT(*) AS total_success,
                    CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS total_amount
                FROM transactions t
                WHERE
                    t.deleted_at IS NULL
                    AND t.status = 'SUCCESS'
                    AND (EXTRACT(YEAR FROM t.created_at) = :year
                         OR EXTRACT(YEAR FROM t.created_at) = :year - 1)
                GROUP BY CAST(EXTRACT(YEAR FROM t.created_at) AS INTEGER)
            ),
            formatted_data AS (
                SELECT
                    CAST(year AS VARCHAR) AS year,
                    CAST(total_success AS INTEGER) AS totalSuccess,
                    CAST(total_amount AS BIGINT) AS totalAmount
                FROM yearly_data
                UNION ALL
                SELECT CAST(:year AS VARCHAR), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = :year)
                UNION ALL
                SELECT CAST((:year - 1) AS VARCHAR), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = :year - 1)
            )
            SELECT * FROM formatted_data
            ORDER BY year DESC
            """, nativeQuery = true)
    List<TransactionYearlyAmountSuccess> findYearlyTransactionSuccess(@Param("year") Integer year);

    @Query(value = """
            WITH monthly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.created_at) AS INTEGER) AS year,
                    CAST(EXTRACT(MONTH FROM t.created_at) AS INTEGER) AS month,
                    COUNT(*) AS total_failed,
                    CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS total_amount
                FROM transactions t
                WHERE
                    t.deleted_at IS NULL
                    AND t.status = 'FAILED'
                    AND (
                        (t.created_at >= make_date(:year, :month, 1)
                         AND t.created_at < (make_date(:year, :month, 1) + INTERVAL '1' MONTH))
                        OR
                        (t.created_at >= make_date(:prevYear, :prevMonth, 1)
                         AND t.created_at < (make_date(:prevYear, :prevMonth, 1) + INTERVAL '1' MONTH))
                    )
                GROUP BY CAST(EXTRACT(YEAR FROM t.created_at) AS INTEGER), CAST(EXTRACT(MONTH FROM t.created_at) AS INTEGER)
            ),
            formatted_data AS (
                SELECT
                    CAST(year AS VARCHAR) AS year,
                    TO_CHAR(TO_DATE(CAST(month AS VARCHAR), 'MM'), 'Mon') AS month,
                    CAST(total_failed AS INTEGER) AS totalFailed,
                    CAST(total_amount AS BIGINT) AS totalAmount
                FROM monthly_data
                UNION ALL
                SELECT CAST(:year AS VARCHAR),
                       TO_CHAR(make_date(:year, :month, 1), 'Mon'),
                       0, 0
                WHERE NOT EXISTS (
                    SELECT 1 FROM monthly_data
                    WHERE year = :year AND month = :month
                )
                UNION ALL
                SELECT CAST(:prevYear AS VARCHAR),
                       TO_CHAR(make_date(:prevYear, :prevMonth, 1), 'Mon'),
                       0, 0
                WHERE NOT EXISTS (
                    SELECT 1 FROM monthly_data
                    WHERE year = :prevYear AND month = :prevMonth
                )
            )
            SELECT * FROM formatted_data
            ORDER BY year DESC, TO_DATE(month, 'Mon') DESC
            """, nativeQuery = true)
    List<TransactionMonthlyAmountFailed> findMonthlyTransactionFailed(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("prevYear") Integer prevYear,
            @Param("prevMonth") Integer prevMonth);

    @Query(value = """
            WITH yearly_data AS (
                SELECT
                    CAST(EXTRACT(YEAR FROM t.created_at) AS INTEGER) AS year,
                    COUNT(*) AS total_failed,
                    CAST(COALESCE(SUM(t.amount), 0) AS BIGINT) AS total_amount
                FROM transactions t
                WHERE
                    t.deleted_at IS NULL
                    AND t.status = 'FAILED'
                    AND (EXTRACT(YEAR FROM t.created_at) = :year
                         OR EXTRACT(YEAR FROM t.created_at) = :year - 1)
                GROUP BY CAST(EXTRACT(YEAR FROM t.created_at) AS INTEGER)
            ),
            formatted_data AS (
                SELECT
                    CAST(year AS VARCHAR) AS year,
                    CAST(total_failed AS INTEGER) AS totalFailed,
                    CAST(total_amount AS BIGINT) AS totalAmount
                FROM yearly_data
                UNION ALL
                SELECT CAST(:year AS VARCHAR), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = :year)
                UNION ALL
                SELECT CAST((:year - 1) AS VARCHAR), 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = :year - 1)
            )
            SELECT * FROM formatted_data
            ORDER BY year DESC
            """, nativeQuery = true)
    List<TransactionYearlyAmountFailed> findYearlyTransactionFailed(@Param("year") Integer year);
}
