package com.sanedge.pointofsale.transaction.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.transaction.models.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;

@Repository
public class TransactionCommandRepositoryImpl implements TransactionCommandRepositoryCustom {

        @PersistenceContext
        private EntityManager em;

        @Override
        @Transactional
        public Transaction trashed(Long transactionId) {
                Transaction entity = em.find(Transaction.class, transactionId);
                if (entity != null && entity.getDeletedAt() == null) {
                        entity.setDeletedAt(new Timestamp(System.currentTimeMillis()));
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Transaction restore(Long transactionId) {
                Transaction entity = em.find(Transaction.class, transactionId);
                if (entity != null && entity.getDeletedAt() != null) {
                        entity.setDeletedAt(null);
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Transaction deletePermanent(Long transactionId) {
                Transaction entity = em.find(Transaction.class, transactionId);
                if (entity != null && entity.getDeletedAt() != null) {
                        em.remove(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public boolean restoreAllDeleted() {
                int updated = em.createQuery(
                                "UPDATE Transaction e SET e.deletedAt = null WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return updated > 0;
        }

        @Override
        @Transactional
        public boolean deleteAllDeleted() {
                int deleted = em.createQuery("DELETE FROM Transaction e WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return deleted > 0;
        }
}
