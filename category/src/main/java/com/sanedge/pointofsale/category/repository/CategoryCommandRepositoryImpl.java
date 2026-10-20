package com.sanedge.pointofsale.category.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.category.models.Category;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;

@Repository
public class CategoryCommandRepositoryImpl implements CategoryCommandRepositoryCustom {

        @PersistenceContext
        private EntityManager em;

        @Override
        @Transactional
        public Category trashed(Long categoryId) {
                Category entity = em.find(Category.class, categoryId);
                if (entity != null && entity.getDeletedAt() == null) {
                        entity.setDeletedAt(new Timestamp(System.currentTimeMillis()));
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Category restore(Long categoryId) {
                Category entity = em.find(Category.class, categoryId);
                if (entity != null && entity.getDeletedAt() != null) {
                        entity.setDeletedAt(null);
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Category deletePermanent(Long categoryId) {
                Category entity = em.find(Category.class, categoryId);
                if (entity != null && entity.getDeletedAt() != null) {
                        em.remove(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public boolean restoreAllDeleted() {
                int updated = em.createQuery("UPDATE Category e SET e.deletedAt = null WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return updated > 0;
        }

        @Override
        @Transactional
        public boolean deleteAllDeleted() {
                int deleted = em.createQuery("DELETE FROM Category e WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return deleted > 0;
        }
}
