package com.sanedge.pointofsale.merchant.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.merchant.models.MerchantDocument;

/**
 * Read side for merchant documents. Port of the blueprint's
 * {@code MerchantDocumentQueryRepository} onto Spring Data paging.
 */
@Repository
public interface MerchantDocumentQueryRepository extends JpaRepository<MerchantDocument, Long> {

    @Query("""
            SELECT d FROM MerchantDocument d
            WHERE (:keyword IS NULL OR
                LOWER(d.documentType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(d.documentUrl) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(d.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(d.note) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY d.documentId ASC
            """)
    Page<MerchantDocument> findDocuments(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT d FROM MerchantDocument d
            WHERE d.deletedAt IS NULL
            AND (:keyword IS NULL OR
                LOWER(d.documentType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(d.documentUrl) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(d.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(d.note) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY d.documentId ASC
            """)
    Page<MerchantDocument> findActiveDocuments(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT d FROM MerchantDocument d
            WHERE d.deletedAt IS NOT NULL
            AND (:keyword IS NULL OR
                LOWER(d.documentType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(d.documentUrl) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(d.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                LOWER(d.note) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY d.documentId DESC
            """)
    Page<MerchantDocument> findTrashedDocuments(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM merchant_documents WHERE document_id = :documentId AND deleted_at IS NULL LIMIT 1", nativeQuery = true)
    Optional<MerchantDocument> findDocumentById(@Param("documentId") Long documentId);
}
