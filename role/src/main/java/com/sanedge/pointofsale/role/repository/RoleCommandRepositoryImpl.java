package com.sanedge.pointofsale.role.repository;

import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.role.models.Role;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;

@Repository
public class RoleCommandRepositoryImpl implements RoleCommandRepositoryCustom {

        @PersistenceContext
        private EntityManager em;

        @Override
        @Transactional
        public Role trashed(Long roleId) {
                Role entity = em.find(Role.class, roleId);
                if (entity != null && entity.getDeletedAt() == null) {
                        entity.setDeletedAt(new Timestamp(System.currentTimeMillis()));
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Role restore(Long roleId) {
                Role entity = em.find(Role.class, roleId);
                if (entity != null && entity.getDeletedAt() != null) {
                        entity.setDeletedAt(null);
                        entity = em.merge(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public Role deletePermanent(Long roleId) {
                Role entity = em.find(Role.class, roleId);
                if (entity != null && entity.getDeletedAt() != null) {
                        em.remove(entity);
                }
                return entity;
        }

        @Override
        @Transactional
        public boolean restoreAllDeleted() {
                int updated = em.createQuery("UPDATE Role e SET e.deletedAt = null WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return updated > 0;
        }

        @Override
        @Transactional
        public boolean deleteAllDeleted() {
                int deleted = em.createQuery("DELETE FROM Role e WHERE e.deletedAt IS NOT NULL")
                                .executeUpdate();
                return deleted > 0;
        }
}
