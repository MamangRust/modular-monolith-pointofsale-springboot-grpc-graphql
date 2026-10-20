package com.sanedge.email;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Email worker (single Kafka consumer). Notifications are published by domain
 * services via the transactional outbox, consumed here and sent through SMTP.
 *
 * <p>Delivery guarantees:
 * <ul>
 *   <li><b>Dedup</b> — {@link EmailDedupGuard} claims
 *       {@code email:idempotency:<event_id>} atomically with a lease BEFORE the
 *       send and flips it to {@code SENT} after a successful delivery;
 *       replays/retries of the same event_id are skipped.</li>
 *   <li><b>Retry / DLQ</b> — a failed SMTP send is re-published to
 *       {@link #RETRY_TOPIC} with a backoff delay and an attempt counter; after
 *       {@code app.kafka.email.retry.max-attempts} the record is moved to
 *       {@link #DLQ_TOPIC} instead of being lost.</li>
 *   <li><b>Ordering</b> — manual offset commits; a record is only committed
 *       after it is fully processed, so a transient failure never lets a later
 *       offset in the same partition be committed first.</li>
 * </ul>
 */
@Component
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public static final String RETRY_TOPIC = "email-service-topic-email-retry";
    public static final String DLQ_TOPIC = "email-service-topic-email-dlq";

    private static final int EVENT_SCHEMA_VERSION = 1;
    private static final List<String> INTERNAL_FIELDS = List.of(
            "_srcTopic", "_srcPartition", "_srcOffset", "_attempt", "_retryAt", "_reason", "_attempts");

    public static final List<String> TOPICS = List.of(
            "email-service-topic-auth-register",
            "email-service-topic-auth-forgot-password",
            "email-service-topic-auth-verify-code-success",
            "email-service-topic-transaction-create",
            "email-service-topic-merchant-create",
            "email-service-topic-merchant-update-status",
            "email-service-topic-merchant-document-create",
            "email-service-topic-merchant-document-update-status");

    private final JavaMailSender mailSender;
    private final EmailDedupGuard dedupGuard;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EmailMetrics metrics;
    private final EmailProperties properties;
    private final ObjectMapper objectMapper;
    private final String mailFrom;

    private final Map<String, Long> partitionLag = new ConcurrentHashMap<>();

    public EmailService(JavaMailSender mailSender,
            EmailDedupGuard dedupGuard,
            KafkaTemplate<String, String> kafkaTemplate,
            EmailMetrics metrics,
            EmailProperties properties,
            ObjectMapper objectMapper,
            @Value("${app.mail.from:no-reply@payment-gateway.com}") String mailFrom) {
        this.mailSender = mailSender;
        this.dedupGuard = dedupGuard;
        this.kafkaTemplate = kafkaTemplate;
        this.metrics = metrics;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.mailFrom = mailFrom;
    }

    @KafkaListener(topics = {
            "email-service-topic-auth-register",
            "email-service-topic-auth-forgot-password",
            "email-service-topic-auth-verify-code-success",
            "email-service-topic-transaction-create",
            "email-service-topic-merchant-create",
            "email-service-topic-merchant-update-status",
            "email-service-topic-merchant-document-create",
            "email-service-topic-merchant-document-update-status"
    }, groupId = "${app.kafka.email.group-id:email-service-group}",
            containerFactory = "manualAckListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            JsonMap payload = parse(record.value());
            processRecord(record.topic(), record.partition(), record.offset(), record.key(), payload, 0);
            recordCommittedPosition(record.topic(), record.partition(), record.offset() + 1);
            ack.acknowledge();
        } catch (Exception error) {
            // Never commit a failed record: Kafka commits are monotonic per
            // partition, so committing would acknowledge the failure too.
            log.error("Email record was not committed; will be redelivered topic={} partition={} offset={}",
                    record.topic(), record.partition(), record.offset(), error);
        }
    }

    /**
     * Process one logical notification. Dedup is checked first; a successful
     * send claims the dedup key; a failed send is routed to retry (or DLQ once
     * max attempts is reached). Package-private so {@link RetryProcessor} can
     * re-enter with the original source coordinates and the attempt counter.
     */
    void processRecord(String srcTopic, int srcPartition, long srcOffset, String key,
            JsonMap payload, int attempt) {
        long startNanos = System.nanoTime();
        try {
            processRecordInternal(srcTopic, srcPartition, srcOffset, key, payload, attempt);
        } finally {
            metrics.recordProcessingDuration((System.nanoTime() - startNanos) / 1_000_000_000.0);
        }
    }

    private void processRecordInternal(String srcTopic, int srcPartition, long srcOffset, String key,
            JsonMap payload, int attempt) {
        if (payload == null) {
            log.warn("Received null payload, skipping");
            return;
        }
        if (!hasValidEnvelope(payload)) {
            // Event contract: invalid events never reach SMTP. They are parked
            // in the DLQ instead of being silently skipped.
            log.warn("Invalid email envelope, routing to DLQ | topic={} partition={} offset={}",
                    srcTopic, srcPartition, srcOffset);
            metrics.recordInvalid();
            sendDeadLetter(payload, key, srcTopic, srcPartition, srcOffset, 0, "invalid_event_envelope");
            return;
        }

        String eventId = payload.getString("event_id");
        switch (dedupGuard.claim(eventId)) {
            case DUPLICATE:
                // Terminal SENT state — replay/retry of the same event_id must
                // never send a second email.
                log.info("Duplicate email skipped (event_id={})", eventId);
                metrics.recordDuplicate();
                return;
            case BUSY:
                // Another consumer holds the lease. Do NOT commit: fail so the
                // retry re-claims once the lease expires.
                throw new IllegalStateException("Email idempotency claim busy, retrying event_id=" + eventId);
            default: // CLAIMED
                try {
                    sendEmail(payload);
                    dedupGuard.markSent(eventId);
                } catch (RuntimeException error) {
                    // Failed send: release the claim so the retry topic can
                    // process the event again.
                    dedupGuard.release(eventId);
                    routeToRetryOrDlq(payload, key, srcTopic, srcPartition, srcOffset, attempt, error);
                }
        }
    }

    private void routeToRetryOrDlq(JsonMap payload, String key, String srcTopic,
            int srcPartition, long srcOffset, int attempt, Throwable error) {
        int nextAttempt = attempt + 1;
        if (nextAttempt >= properties.getRetryMaxAttempts()) {
            sendDeadLetter(payload, key, srcTopic, srcPartition, srcOffset, nextAttempt, "max_retries_exceeded");
            return;
        }
        metrics.recordRetried();

        JsonMap retry = payload.copy()
                .put("_srcTopic", srcTopic)
                .put("_srcPartition", srcPartition)
                .put("_srcOffset", srcOffset)
                .put("_attempt", nextAttempt)
                .put("_retryAt", System.currentTimeMillis() + backoffMs(nextAttempt))
                .put("_reason", "smtp_send_failed");

        log.warn("Email send failed; scheduled retry {} of {} | to={}",
                nextAttempt, properties.getRetryMaxAttempts(), payload.getString("email"));
        sendToTopic(RETRY_TOPIC, key, retry);
    }

    private void sendEmail(JsonMap payload) {
        String email = payload.getString("email");
        if (email == null || payload.getString("subject") == null || payload.getString("body") == null) {
            log.warn("Received incomplete email payload (event_id={})", payload.getString("event_id"));
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject(payload.getString("subject"));
        message.setText(payload.getString("body"));
        mailSender.send(message);
        metrics.recordSent();
        log.info("Email successfully sent to {}", email);
    }

    private void sendDeadLetter(JsonMap payload, String key, String srcTopic,
            int srcPartition, long srcOffset, int attempts, String reason) {
        JsonMap dlq = payload == null ? new JsonMap() : payload.copy();
        for (String field : INTERNAL_FIELDS) {
            dlq.remove(field);
        }
        dlq.put("_reason", reason)
                .put("_srcTopic", srcTopic)
                .put("_srcPartition", srcPartition)
                .put("_srcOffset", srcOffset)
                .put("_attempts", attempts);

        kafkaTemplate.send(DLQ_TOPIC, key, dlq.encode());
        metrics.recordDlqSent();
        log.error("Email moved to DLQ after {} attempts | to={} topic={} reason={}",
                attempts, payload == null ? null : payload.getString("email"), srcTopic, reason);
    }

    private void sendToTopic(String topic, String key, JsonMap payload) {
        kafkaTemplate.send(topic, key, payload.encode());
    }

    private long backoffMs(int attempt) {
        return Math.min(attempt * properties.getRetryBackoffMs(), 300_000L);
    }

    /** Periodically refreshes the per-partition lag gauge. */
    @Scheduled(fixedDelayString = "${app.kafka.email.lag.poll-ms:15000}")
    public void refreshLag() {
        metrics.refreshKafkaLag(new HashMap<>(partitionLag));
    }

    void recordCommittedPosition(String topic, int partition, long nextOffset) {
        partitionLag.put(topic + "-" + partition, nextOffset);
    }

    private boolean hasValidEnvelope(JsonMap payload) {
        return payload != null
                && payload.getString("event_id") != null
                && payload.getInt("schema_version", 0) == EVENT_SCHEMA_VERSION
                && payload.getString("event_type") != null
                && payload.getString("email") != null
                && payload.getString("subject") != null
                && payload.getString("body") != null;
    }

    private JsonMap parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {
            });
            return new JsonMap(map);
        } catch (Exception error) {
            log.warn("Failed to parse Kafka record payload: {}", error.getMessage());
            return null;
        }
    }
}
