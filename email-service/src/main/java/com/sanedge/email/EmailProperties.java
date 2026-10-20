package com.sanedge.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Email worker tuning knobs, mapped onto the previous Quarkus
 * {@code email.retry.*} / {@code email.idempotency.*} configuration so the
 * existing {@code EMAIL_*} env vars keep working.
 */
@Component
@ConfigurationProperties(prefix = "app.kafka.email")
public class EmailProperties {

    /** Consumer group for the main email topic set. */
    private String groupId = "email-service-group";

    /** Consumer group for the retry topic. */
    private String retryGroupId = "email-service-retry-group";

    /** Maximum send attempts before an event is routed to the DLQ. */
    private int retryMaxAttempts = 3;

    /** Base backoff (ms) between retry attempts. */
    private long retryBackoffMs = 30000;

    /** How often (ms) the per-partition lag gauge is refreshed. */
    private long lagPollMs = 15000;

    private final Idempotency idempotency = new Idempotency();

    public static class Idempotency {
        /** Short lease (seconds) held while an event is being processed. */
        private long leaseSeconds = 60;
        /** Terminal dedup TTL (seconds) once an event is marked SENT. */
        private long ttlSeconds = 86400;

        public long getLeaseSeconds() {
            return leaseSeconds;
        }

        public void setLeaseSeconds(long leaseSeconds) {
            this.leaseSeconds = leaseSeconds;
        }

        public long getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(long ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getRetryGroupId() {
        return retryGroupId;
    }

    public void setRetryGroupId(String retryGroupId) {
        this.retryGroupId = retryGroupId;
    }

    public int getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    public void setRetryMaxAttempts(int retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }

    public long getRetryBackoffMs() {
        return retryBackoffMs;
    }

    public void setRetryBackoffMs(long retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }

    public long getLagPollMs() {
        return lagPollMs;
    }

    public void setLagPollMs(long lagPollMs) {
        this.lagPollMs = lagPollMs;
    }

    public Idempotency getIdempotency() {
        return idempotency;
    }
}
