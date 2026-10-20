package com.sanedge.pointofsale.common.outbox;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transactional outbox row (shared {@code outbox} table, migration V9).
 *
 * <p>Port of the blueprint's per-module {@code Outbox} entity. All modules write
 * the same table and are distinguished by {@code aggregateType}; the shared
 * {@link OutboxPublisher} drains PENDING rows regardless of owner.
 *
 * <p>Does not extend {@code BaseModel}: the V9 table has only {@code created_at}
 * (no {@code updated_at}/{@code deleted_at}), matching the blueprint entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outbox")
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Logical producer, e.g. {@code USER} or {@code TRANSACTION}. */
    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    /** Kafka record key. */
    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(nullable = false, length = 200)
    private String topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "processed_at")
    private Timestamp processedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = new Timestamp(System.currentTimeMillis());
        }
    }
}
