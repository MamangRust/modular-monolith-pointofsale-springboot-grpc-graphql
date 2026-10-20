package com.sanedge.pointofsale.merchant.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.merchant.models.MerchantDocument;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/** Native soft-delete statements, mirroring the merchant repository impl. */
@Repository
public class MerchantDocumentCommandRepositoryImpl implements MerchantDocumentCommandRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private static final String TRASHED_QUERY = """
            UPDATE merchant_documents
            SET deleted_at = CURRENT_TIMESTAMP
            WHERE document_id = :documentId
              AND deleted_at IS NULL
            """;

    private static final String RESTORE_QUERY = """
            UPDATE merchant_documents
            SET deleted_at = NULL
            WHERE document_id = :documentId
              AND deleted_at IS NOT NULL
            """;

    private static final String DELETE_PERMANENT_QUERY = """
            DELETE FROM merchant_documents
            WHERE document_id = :documentId
              AND deleted_at IS NOT NULL
            """;

    private static final String RESTORE_ALL_QUERY = """
            UPDATE merchant_documents
            SET deleted_at = NULL
            WHERE deleted_at IS NOT NULL
            """;

    private static final String DELETE_ALL_QUERY = """
            DELETE FROM merchant_documents
            WHERE deleted_at IS NOT NULL
            """;

    @Override
    @Transactional
    public MerchantDocument trashed(Long documentId) {
        MerchantDocument doc = em.find(MerchantDocument.class, documentId);

        if (doc != null) {
            em.createNativeQuery(TRASHED_QUERY)
                    .setParameter("documentId", documentId)
                    .executeUpdate();

            em.refresh(doc);
        }

        return doc;
    }

    @Override
    @Transactional
    public MerchantDocument restore(Long documentId) {
        MerchantDocument doc = em.find(MerchantDocument.class, documentId);

        if (doc != null) {
            em.createNativeQuery(RESTORE_QUERY)
                    .setParameter("documentId", documentId)
                    .executeUpdate();

            em.refresh(doc);
        }

        return doc;
    }

    @Override
    @Transactional
    public MerchantDocument deletePermanent(Long documentId) {
        MerchantDocument doc = em.find(MerchantDocument.class, documentId);

        if (doc != null) {
            int deleted = em.createNativeQuery(DELETE_PERMANENT_QUERY)
                    .setParameter("documentId", documentId)
                    .executeUpdate();

            em.detach(doc);

            return deleted > 0 ? doc : null;
        }

        return null;
    }

    @Override
    @Transactional
    public boolean restoreAllDeleted() {
        int updated = em.createNativeQuery(RESTORE_ALL_QUERY)
                .executeUpdate();

        return updated > 0;
    }

    @Override
    @Transactional
    public boolean deleteAllDeleted() {
        int deleted = em.createNativeQuery(DELETE_ALL_QUERY)
                .executeUpdate();

        return deleted > 0;
    }
}
