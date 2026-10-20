package com.sanedge.pointofsale.common.outbox;

/**
 * Lifecycle of a row in the shared {@code outbox} table (migration V9).
 *
 * <p>Port of the blueprint's {@code OutboxStatus}. Unlike the ecommerce
 * per-module outboxes there is no lease: the publisher increments
 * {@code attempts} and, once {@code max-attempts} is reached, parks the row as
 * {@link #FAILED} (dead letter) instead of retrying forever.
 */
public enum OutboxStatus {
    PENDING,
    PROCESSED,
    FAILED
}
