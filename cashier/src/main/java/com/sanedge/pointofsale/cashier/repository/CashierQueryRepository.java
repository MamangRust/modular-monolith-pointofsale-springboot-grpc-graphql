package com.sanedge.pointofsale.cashier.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.cashier.models.Cashier;

@Repository
public interface CashierQueryRepository extends JpaRepository<Cashier, Long> {
        @Query("""
                        SELECT c FROM Cashier c
                        WHERE (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                        ORDER BY c.createdAt ASC
                        """)
        Page<Cashier> findAllCashiers(
                        @Param("keyword") String keyword,
                        Pageable pageable);

        @Query("""
                        SELECT c FROM Cashier c
                        WHERE c.deletedAt IS NULL
                          AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                          AND (:merchantId IS NULL OR c.merchantId = :merchantId)
                        ORDER BY c.createdAt ASC
                        """)
        Page<Cashier> findByMerchants(
                        @Param("merchantId") Long merchantId,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        @Query("""
                        SELECT c FROM Cashier c
                        WHERE c.deletedAt IS NULL
                          AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                        ORDER BY c.createdAt ASC
                        """)
        Page<Cashier> findActiveCashiers(
                        @Param("keyword") String keyword,
                        Pageable pageable);

        @Query("""
                        SELECT c FROM Cashier c
                        WHERE c.deletedAt IS NOT NULL
                          AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
                        ORDER BY c.deletedAt DESC
                        """)
        Page<Cashier> findTrashedCashiers(
                        @Param("keyword") String keyword,
                        Pageable pageable);

        @Query("""
                        SELECT c FROM Cashier c
                        WHERE c.cashierId = :cashierId
                        """)
        Optional<Cashier> findByCashierId(@Param("cashierId") Long cashierId);

        @Query("""
                            SELECT c FROM Cashier c
                            WHERE LOWER(c.name) = LOWER(:name)
                              AND c.merchantId = :merchantId
                              AND c.deletedAt IS NULL
                        """)
        Optional<Cashier> findByNameAndMerchantId(@Param("name") String name,
                        @Param("merchantId") Long merchantId);
}
