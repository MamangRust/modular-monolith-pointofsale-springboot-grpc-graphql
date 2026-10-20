package com.sanedge.pointofsale.common.outbox;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Shared Kafka producer for the outbox relay.
 *
 * <p>Registered in {@code common} (gated on spring-kafka being present and no
 * module already defining a producer) so every producer module gets the same
 * tuned, idempotent producer without duplicating this config. Port of the
 * blueprint's {@code KafkaService} producer settings
 * ({@code KafkaConfig.producer(...)}).
 */
@Configuration
@ConditionalOnClass(KafkaTemplate.class)
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.acks:all}")
    private String acks;

    @Value("${kafka.enable.idempotence:true}")
    private boolean enableIdempotence;

    @Value("${kafka.retries:10}")
    private int retries;

    @Value("${kafka.retry.backoff.ms:500}")
    private long retryBackoffMs;

    @Value("${kafka.max.in.flight.requests.per.connection:5}")
    private int maxInFlightRequests;

    @Value("${kafka.delivery.timeout.ms:120000}")
    private int deliveryTimeoutMs;

    @Value("${kafka.request.timeout.ms:30000}")
    private int requestTimeoutMs;

    @Value("${kafka.compression.type:lz4}")
    private String compressionType;

    @Value("${kafka.linger.ms:5}")
    private int lingerMs;

    @Value("${kafka.batch.size:16384}")
    private int batchSize;

    @Value("${kafka.max.block.ms:60000}")
    private int maxBlockMs;

    @Bean
    @ConditionalOnMissingBean(ProducerFactory.class)
    public ProducerFactory<String, String> producerFactory() {
        validate();

        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, acks);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        config.put(ProducerConfig.RETRIES_CONFIG, retries);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequests);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        config.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, maxBlockMs);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    @ConditionalOnMissingBean(KafkaTemplate.class)
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /** Mirrors the blueprint's fail-fast configuration checks. */
    private void validate() {
        if (enableIdempotence && !"all".equalsIgnoreCase(acks)) {
            throw new IllegalStateException("Kafka idempotence requires acks=all");
        }
        if (enableIdempotence && (maxInFlightRequests < 1 || maxInFlightRequests > 5)) {
            throw new IllegalStateException(
                    "Kafka max.in.flight.requests.per.connection must be between 1 and 5 when idempotence is enabled");
        }
        if (retries < 0 || retryBackoffMs < 0 || requestTimeoutMs <= 0 || deliveryTimeoutMs <= 0) {
            throw new IllegalStateException("Kafka retry and timeout settings must be positive");
        }
        if (lingerMs < 0 || batchSize <= 0) {
            throw new IllegalStateException("Kafka linger.ms must be >= 0 and batch.size must be > 0");
        }
    }
}
