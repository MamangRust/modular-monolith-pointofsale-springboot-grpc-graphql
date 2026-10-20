package com.sanedge.pointofsale.merchant.repository;

import java.sql.Timestamp;

import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.merchant.models.Merchant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class MerchantCommandRepositoryImpl implements MerchantCommandRepositoryCustom {

        @PersistenceContext
        private EntityManager em;

        @Override
        @Transactional
        public Merchant trashed(Long merchantId) {
                Merchant entity = em.find(Merchant.class, merchantId);
                if (entity != null && entity.getDeletedAt() == null) {
                        entity.setDeletedAt(new Timestamp(System.currentTimeMillis()));
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Merchant restore(Long merchantId) {
                Merchant entity = em.find(Merchant.class, merchantId);
                if (entity != null && entity.getDeletedAt() != null) {
                        entity.setDeletedAt(null);
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public boolean deletePermanent(Long merchantId) {
                Merchant entity = em.find(Merchant.class, merchantId);
                if (entity != null && entity.getDeletedAt() != null) {
                        em.remove(entity);
                }
                return true;
        }

        @Override
        @Transactional
        public boolean restoreAllDeleted() {
                int updated = em.createQuery("UPDATE Merchant e SET e.deletedAt = null WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return updated > 0;
        }

        @Override
        @Transactional
        public boolean deleteAllDeleted() {
                int deleted = em.createQuery("DELETE FROM Merchant e WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return deleted > 0;
        }
}
