package com.sanedge.pointofsale.cashier.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.cashier.models.Cashier;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;

@Repository
public class CashierCommandRepositoryImpl implements CashierCommandRepositoryCustom {

        @PersistenceContext
        private EntityManager em;

        @Override
        @Transactional
        public Cashier trashed(Long cashierId) {
                Cashier entity = em.find(Cashier.class, cashierId);
                if (entity != null && entity.getDeletedAt() == null) {
                        entity.setDeletedAt(new Timestamp(System.currentTimeMillis()));
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Cashier restore(Long cashierId) {
                Cashier entity = em.find(Cashier.class, cashierId);
                if (entity != null && entity.getDeletedAt() != null) {
                        entity.setDeletedAt(null);
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Cashier deletePermanent(Long cashierId) {
                Cashier entity = em.find(Cashier.class, cashierId);
                if (entity != null && entity.getDeletedAt() != null) {
                        em.remove(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public boolean restoreAllDeleted() {
                int updated = em.createQuery("UPDATE Cashier e SET e.deletedAt = null WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return updated > 0;
        }

        @Override
        @Transactional
        public boolean deleteAllDeleted() {
                int deleted = em.createQuery("DELETE FROM Cashier e WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return deleted > 0;
        }
}
