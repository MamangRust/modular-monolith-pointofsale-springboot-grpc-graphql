package com.sanedge.pointofsale.common.outbox;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sanedge.pointofsale.common.chaos.ChaosManager;
import com.sanedge.pointofsale.common.chaos.ChaosPolicy;

/**
 * Drains the shared {@code outbox} table into Kafka.
 *
 * <p>Port of the blueprint's per-module {@code OutboxPublisher}. Unlike the
 * ecommerce relay it does <em>not</em> claim/lease rows: it reads PENDING rows,
 * publishes each, and on failure increments {@code attempts} — after
 * {@code max-attempts} the row becomes {@code FAILED} (dead letter).
 *
 * <p>The scheduler runs on Spring's default single-threaded scheduler; a boolean
 * guard prevents overlapping ticks.
 *
 * <p>Gated on {@link KafkaTemplate} so non-producer modules do not load this bean.
 * Its {@code KafkaTemplate} field would otherwise be introspected by Spring on
 * every scan of this package, failing the whole context with
 * {@code NoClassDefFoundError} in modules that lack spring-kafka.
 */
@Component
@ConditionalOnClass(KafkaTemplate.class)
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ChaosManager chaosManager;

    @Value("${outbox.publisher.batch-size:50}")
    private int batchSize;

    @Value("${outbox.publisher.max-attempts:5}")
    private int maxAttempts;

    private volatile boolean running;

    public OutboxPublisher(OutboxRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            ChaosManager chaosManager) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.chaosManager = chaosManager;
    }

    /** Default interval matches the blueprint's 5000 ms. */
    @Scheduled(fixedDelayString = "${outbox.publisher.interval-ms:5000}",
            initialDelayString = "${outbox.publisher.initial-delay-ms:5000}")
    public void poll() {
        if (running) {
            return; // previous tick still running
        }
        running = true;
        try {
            drain();
        } catch (RuntimeException e) {
            log.error("❌ Outbox publisher tick failed: {}", e.getMessage(), e);
        } finally {
            running = false;
        }
    }

    /** Fetches PENDING rows and publishes them sequentially. */
    void drain() {
        List<Outbox> pending = repository.findPending(Math.max(1, batchSize));
        for (Outbox event : pending) {
            publishOne(event);
        }
    }

    private void publishOne(Outbox event) {
        try {
            send(event);
            repository.markProcessed(event.getId());
            log.debug("📤 Outbox published | id={} topic={} key={}",
                    event.getId(), event.getTopic(), event.getAggregateId());
        } catch (RuntimeException error) {
            log.warn("⚠️ Outbox publish failed | id={} topic={} error={}",
                    event.getId(), event.getTopic(), error.getMessage());
            repository.markFailed(event.getId(), error.getMessage(), Math.max(1, maxAttempts));

            int attempts = event.getAttempts() == null ? 1 : event.getAttempts() + 1;
            if (attempts >= Math.max(1, maxAttempts)) {
                log.error("❌ Outbox moved to FAILED (dead letter) | id={} topic={} attempts={}",
                        event.getId(), event.getTopic(), attempts);
            }
        }
    }

    /** Publishes the payload, honouring {@code kafka}-type chaos policies. */
    private void send(Outbox event) {
        ChaosPolicy policy = chaosManager == null ? null : chaosManager.evaluate("kafka", event.getTopic());
        if (policy != null && policy.isEnabled() && Math.random() < policy.getErrorChance()) {
            log.info("Injecting Kafka chaos policy={} topic={}", policy.getName(), event.getTopic());

            if (policy.getLatencyMs() > 0) {
                try {
                    Thread.sleep(policy.getLatencyMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (policy.isDropMessage()) {
                throw new IllegalStateException("Simulated Kafka drop for topic " + event.getTopic());
            }
            if (policy.isRejectMessage()) {
                throw new IllegalStateException(policy.getErrorMessage() != null
                        ? policy.getErrorMessage()
                        : "Simulated Kafka rejection for topic " + event.getTopic());
            }
        }

        kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload()).join();
    }
}
