package com.sanedge.pointofsale.merchant.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.merchant.models.Merchant;

@Repository
public interface MerchantQueryRepository extends JpaRepository<Merchant, Long> {
    @Query("""
            SELECT m FROM Merchant m
            WHERE (:keyword IS NULL
                OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.contactEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.contactPhone) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY m.createdAt ASC
            """)
    Page<Merchant> findMerchants(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT m FROM Merchant m
            WHERE m.deletedAt IS NULL
            AND (:keyword IS NULL
                OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.contactEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.contactPhone) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY m.createdAt ASC
            """)
    Page<Merchant> findActiveMerchants(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT m FROM Merchant m
            WHERE m.deletedAt IS NOT NULL
            AND (:keyword IS NULL
                OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.contactEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.contactPhone) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY m.deletedAt DESC
            """)
    Page<Merchant> findTrashedMerchants(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT m FROM Merchant m WHERE m.merchantId = :merchantId and m.deletedAt is NULL
            """)
    Optional<Merchant> findMerchantById(@Param("merchantId") Long merchantId);

    Optional<Merchant> findByUserId(Long userId);
}
