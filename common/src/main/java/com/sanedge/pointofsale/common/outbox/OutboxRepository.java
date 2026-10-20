package com.sanedge.pointofsale.common.outbox;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Persistence for the shared {@code outbox} table.
 *
 * <p>Port of the blueprint's per-module {@code OutboxRepository}. Unlike the
 * ecommerce outboxes there is no claim/lease — a single publisher instance
 * drains rows, increments {@code attempts}, and moves a row to {@code FAILED}
 * once {@code maxAttempts} is reached.
 */
@Repository
public class OutboxRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /** Persists a new PENDING event; call inside the business transaction. */
    @Transactional
    public void enqueue(Outbox event) {
        entityManager.persist(event);
    }

    @Transactional(readOnly = true)
    public List<Outbox> findPending(int limit) {
        return entityManager.createQuery(
                "SELECT o FROM Outbox o WHERE o.status = :status ORDER BY o.createdAt", Outbox.class)
                .setParameter("status", OutboxStatus.PENDING)
                .setMaxResults(limit)
                .getResultList();
    }

    @Transactional
    public void markProcessed(Long id) {
        entityManager.createQuery(
                "UPDATE Outbox o SET o.status = :status, o.processedAt = :now, o.lastError = NULL WHERE o.id = :id")
                .setParameter("status", OutboxStatus.PROCESSED)
                .setParameter("now", Timestamp.valueOf(LocalDateTime.now()))
                .setParameter("id", id)
                .executeUpdate();
    }

    /**
     * Records a delivery failure. {@code attempts} is incremented; once it reaches
     * {@code maxAttempts} the row is parked as {@code FAILED} (dead letter),
     * otherwise it stays {@code PENDING} for the next tick.
     */
    @Transactional
    public void markFailed(Long id, String error, int maxAttempts) {
        Outbox event = entityManager.find(Outbox.class, id);
        if (event == null) {
            return;
        }

        int attempts = event.getAttempts() == null ? 1 : event.getAttempts() + 1;
        event.setAttempts(attempts);
        event.setLastError(error);

        if (attempts >= maxAttempts) {
            event.setStatus(OutboxStatus.FAILED);
            event.setProcessedAt(Timestamp.valueOf(LocalDateTime.now()));
        }
    }

    /** Backlog depth for the backlog gauge / tests. */
    @Transactional(readOnly = true)
    public long countPending() {
        return entityManager.createQuery(
                "SELECT COUNT(o) FROM Outbox o WHERE o.status = :status", Long.class)
                .setParameter("status", OutboxStatus.PENDING)
                .getSingleResult();
    }
}
