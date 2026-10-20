package com.sanedge.pointofsale.product.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.product.models.Product;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;

@Repository
public class ProductCommandRepositoryImpl implements ProductCommandRepositoryCustom {

        @PersistenceContext
        private EntityManager em;

        @Override
        @Transactional
        public Product trashed(Long productId) {
                Product entity = em.find(Product.class, productId);
                if (entity != null && entity.getDeletedAt() == null) {
                        entity.setDeletedAt(new Timestamp(System.currentTimeMillis()));
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Product restore(Long productId) {
                Product entity = em.find(Product.class, productId);
                if (entity != null && entity.getDeletedAt() != null) {
                        entity.setDeletedAt(null);
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Product deletePermanent(Long productId) {
                Product entity = em.find(Product.class, productId);
                if (entity != null && entity.getDeletedAt() != null) {
                        em.remove(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public boolean restoreAllDeleted() {
                int updated = em.createQuery("UPDATE Product e SET e.deletedAt = null WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return updated > 0;
        }

        @Override
        @Transactional
        public boolean deleteAllDeleted() {
                int deleted = em.createQuery("DELETE FROM Product e WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return deleted > 0;
        }
}
