package com.sanedge.pointofsale.common.resilience;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Tuning untuk {@link DependencyGuardInterceptor}. Nilai default meniru
 * konstanta di {@code pkg/resilience/client_interceptor.go} (blueprint Quarkus
 * {@code DependencyGuardConfig}): 5 kegagalan membuka breaker, 30s sampai
 * half-open, maks 100 call konkuren, deadline per-call 3s.
 *
 * <p>Contoh override:
 * <pre>
 * resilience.dependency-guard.failure-threshold=5
 * resilience.dependency-guard.open-timeout-seconds=30
 * resilience.dependency-guard.max-concurrent=100
 * resilience.dependency-guard.call-timeout=3s
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "resilience.dependency-guard")
public class DependencyGuardProperties {

    /** Jumlah kegagalan transport dalam window yang membuka circuit breaker. */
    private long failureThreshold = 5;

    /** Lama (detik) circuit breaker bertahan OPEN sebelum half-open. */
    private long openTimeoutSeconds = 30;

    /** Maksimum call konkuren per method (bulkhead). */
    private int maxConcurrent = 100;

    /** Deadline per-call. */
    private Duration callTimeout = Duration.ofSeconds(3);

    public long getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(long failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public long getOpenTimeoutSeconds() {
        return openTimeoutSeconds;
    }

    public void setOpenTimeoutSeconds(long openTimeoutSeconds) {
        this.openTimeoutSeconds = openTimeoutSeconds;
    }

    public int getMaxConcurrent() {
        return maxConcurrent;
    }

    public void setMaxConcurrent(int maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
    }

    public Duration getCallTimeout() {
        return callTimeout;
    }

    public void setCallTimeout(Duration callTimeout) {
        this.callTimeout = callTimeout;
    }
}
