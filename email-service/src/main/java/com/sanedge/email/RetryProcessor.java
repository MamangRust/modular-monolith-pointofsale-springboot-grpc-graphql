package com.sanedge.email;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Drains {@link EmailService#RETRY_TOPIC}. Records carry the original source
 * coordinates ({@code _srcTopic/_srcPartition/_srcOffset}), the attempt counter
 * ({@code _attempt}) and a {@code _retryAt} timestamp so backoff delays are
 * honoured. Records past {@code app.kafka.email.retry.max-attempts} are routed
 * to the DLQ by {@link EmailService#processRecord}.
 */
@Component
public class RetryProcessor {

    private static final Logger log = LoggerFactory.getLogger(RetryProcessor.class);

    private static final List<String> INTERNAL_FIELDS = List.of(
            "_srcTopic", "_srcPartition", "_srcOffset", "_attempt", "_retryAt", "_reason", "_attempts");

    private final EmailService emailService;

    public RetryProcessor(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = EmailService.RETRY_TOPIC,
            groupId = "${app.kafka.email.retry.group-id:email-service-retry-group}",
            containerFactory = "retryListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        JsonMap payload = record.value() == null || record.value().isBlank()
                ? null
                : JsonMap.fromJson(record.value());
        if (payload == null) {
            ack.acknowledge();
            return;
        }
        try {
            waitUntilRetryAt(payload);

            String srcTopic = payload.getString("_srcTopic");
            int srcPartition = payload.getInt("_srcPartition", 0);
            long srcOffset = payload.getLong("_srcOffset", 0L);
            int attempt = payload.getInt("_attempt", 1);

            JsonMap clean = payload.copy();
            for (String field : INTERNAL_FIELDS) {
                clean.remove(field);
            }

            emailService.processRecord(srcTopic, srcPartition, srcOffset, record.key(), clean, attempt);
            ack.acknowledge();
        } catch (Exception error) {
            // Leave the offset uncommitted so the retry record is redelivered.
            log.error("Retry processing failed | topic={} partition={} offset={} error={}",
                    record.topic(), record.partition(), record.offset(), error.getMessage(), error);
        }
    }

    private void waitUntilRetryAt(JsonMap payload) {
        long delay = payload.getLong("_retryAt", 0L) - System.currentTimeMillis();
        if (delay <= 0) {
            return;
        }
        try {
            Thread.sleep(Math.min(delay, 300_000L));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
