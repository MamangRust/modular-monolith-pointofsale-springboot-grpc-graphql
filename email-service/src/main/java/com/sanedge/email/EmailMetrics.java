package com.sanedge.email;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

/**
 * Micrometer-backed email worker metrics.
 *
 * <p>The original Quarkus worker published these through the OpenTelemetry
 * meter; the Spring Boot port exposes the same logical counters via Micrometer
 * (Prometheus): sent, failed, retried, duplicate, dlq, invalid, plus the
 * processing-duration timer, the idempotency-claim-failure counter and a
 * per-partition consumer-lag gauge.
 */
@Component
public class EmailMetrics {

    private final Counter sent;
    private final Counter failed;
    private final Counter retried;
    private final Counter duplicate;
    private final Counter dlq;
    private final Counter invalid;
    private final Counter claimFailed;
    private final Timer processingDuration;
    private final MultiGauge consumerLag;

    public EmailMetrics(MeterRegistry registry) {
        this.sent = Counter.builder("email_sent_total").register(registry);
        this.failed = Counter.builder("email_failed_total").register(registry);
        this.retried = Counter.builder("email_retried_total").register(registry);
        this.duplicate = Counter.builder("email_duplicate_total").register(registry);
        this.dlq = Counter.builder("email_dlq_total").register(registry);
        this.invalid = Counter.builder("email_invalid_event_total").register(registry);
        this.claimFailed = Counter.builder("email_idempotency_claim_failed_total").register(registry);
        this.processingDuration = Timer.builder("email_processing_duration_seconds").register(registry);
        this.consumerLag = MultiGauge.builder("kafka_consumer_lag")
                .description("Kafka consumer lag per partition for the email consumer group")
                .register(registry);
    }

    /**
     * Refreshes the per-partition lag gauge from the supplied map of
     * {@code topic-partition -> lag}.
     */
    public void refreshKafkaLag(Map<String, Long> lagByPartition) {
        Map<String, Long> source = lagByPartition == null ? new HashMap<>() : lagByPartition;
        List<MultiGauge.Row<?>> rows = new ArrayList<>(source.size());
        for (Map.Entry<String, Long> entry : source.entrySet()) {
            String key = entry.getKey();
            int idx = key.lastIndexOf('-');
            Tags tags = idx > 0
                    ? Tags.of("topic", key.substring(0, idx), "partition", key.substring(idx + 1))
                    : Tags.of("topic", key, "partition", "0");
            rows.add(MultiGauge.Row.of(tags, entry.getValue() == null ? 0L : entry.getValue()));
        }
        consumerLag.register(rows, true);
    }

    /** Convenience alias kept for parity with the Quarkus call-site naming. */
    public void registerKafkaLagGauge(Supplier<Map<String, Long>> lagSupplier) {
        refreshKafkaLag(lagSupplier == null ? null : lagSupplier.get());
    }

    public void recordSent() {
        sent.increment();
    }

    public void recordFailed() {
        failed.increment();
    }

    public void recordRetried() {
        retried.increment();
    }

    public void recordDuplicate() {
        duplicate.increment();
    }

    public void recordDlqSent() {
        dlq.increment();
    }

    public void recordInvalid() {
        invalid.increment();
    }

    public void recordClaimFailed() {
        claimFailed.increment();
    }

    public void recordProcessingDuration(double seconds) {
        processingDuration.record(Duration.ofNanos((long) (seconds * 1_000_000_000L)));
    }
}
