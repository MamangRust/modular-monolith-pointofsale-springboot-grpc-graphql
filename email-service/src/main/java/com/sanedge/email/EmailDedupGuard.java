package com.sanedge.email;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Durable idempotency guard for the email consumer.
 *
 * <p>Every event goes through a small state machine stored in Redis:
 *
 * <pre>
 * ABSENT --claim(SET NX PX lease)--> PROCESSING --send ok--> SENT (TTL 24h)
 *                                         |--send fail--> release (DEL) -> retry/DLQ
 * </pre>
 *
 * <ul>
 *   <li>{@link #claim} is atomic ({@code SET NX PX}): only one consumer can hold
 *       the lease, so two replicas can never send the same event concurrently.</li>
 *   <li>The lease makes crash windows bounded: if a consumer dies after claiming
 *       but before sending, the key expires and the next attempt re-claims it
 *       instead of being blocked for the full dedup TTL.</li>
 *   <li>{@link #markSent} flips the key to a terminal {@code SENT} state so replays
 *       and retry-topic records with the same {@code event_id} are skipped.</li>
 *   <li>Fail-open: if Redis is unavailable the guard lets the email through
 *       (availability of notifications &gt; exactly-once), consistent with the
 *       documented outage policy.</li>
 * </ul>
 */
@Component
public class EmailDedupGuard {

    private static final Logger log = LoggerFactory.getLogger(EmailDedupGuard.class);

    private static final String STATE_PROCESSING = "PROCESSING";
    private static final String STATE_SENT = "SENT";

    /** Result of an atomic {@link #claim}. */
    public enum ClaimResult {
        /** This caller owns the lease and must send the email. */
        CLAIMED,
        /** Event already reached a terminal {@code SENT} state — skip. */
        DUPLICATE,
        /** Another processor holds an active lease — do not send, retry later. */
        BUSY
    }

    private final StringRedisTemplate redis;
    private final EmailMetrics metrics;
    private final EmailProperties properties;

    public EmailDedupGuard(StringRedisTemplate redis, EmailMetrics metrics, EmailProperties properties) {
        this.redis = redis;
        this.metrics = metrics;
        this.properties = properties;
    }

    /**
     * Atomically claims {@code eventId} for processing. See {@link ClaimResult}.
     *
     * <p>The claim mirrors the original {@code SETNX} + {@code EXPIRE}: only one
     * consumer can create the key. If the process crashes between the two steps
     * the key is left without a TTL, so the next claim self-heals it
     * ({@code ttl == -1}) by deleting and re-claiming instead of blocking the
     * event forever.
     */
    public ClaimResult claim(String eventId) {
        String key = key(eventId);
        long leaseSeconds = properties.getIdempotency().getLeaseSeconds();
        try {
            Boolean created = redis.opsForValue()
                    .setIfAbsent(key, STATE_PROCESSING, Duration.ofSeconds(leaseSeconds));
            if (Boolean.TRUE.equals(created)) {
                return ClaimResult.CLAIMED;
            }
            String state = redis.opsForValue().get(key);
            if (STATE_SENT.equals(state)) {
                return ClaimResult.DUPLICATE;
            }
            // PROCESSING (or unknown value): check lease liveness.
            Long ttl = redis.getExpire(key, java.util.concurrent.TimeUnit.SECONDS);
            if (ttl != null && ttl == -1L) {
                // Orphaned PROCESSING key (crash between setnx and expire) — reclaim it.
                redis.delete(key);
                return claim(eventId);
            }
            return ClaimResult.BUSY;
        } catch (RuntimeException error) {
            metrics.recordClaimFailed();
            log.warn("Idempotency claim failed (fail-open), sending anyway | event_id={} error={}",
                    eventId, error.getMessage());
            return ClaimResult.CLAIMED;
        }
    }

    /** Marks the event as successfully delivered (terminal state, full dedup TTL). */
    public void markSent(String eventId) {
        try {
            redis.opsForValue().set(key(eventId), STATE_SENT,
                    Duration.ofSeconds(properties.getIdempotency().getTtlSeconds()));
        } catch (RuntimeException error) {
            log.warn("Idempotency markSent failed (fail-open) | event_id={} error={}",
                    eventId, error.getMessage());
        }
    }

    /** Releases the claim so a failed send can be retried. */
    public void release(String eventId) {
        try {
            redis.delete(key(eventId));
        } catch (RuntimeException error) {
            log.warn("Idempotency release failed (fail-open) | event_id={} error={}",
                    eventId, error.getMessage());
        }
    }

    private String key(String eventId) {
        return "email:idempotency:" + eventId;
    }
}
