package com.sanedge.email;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * Spring Kafka wiring for the email worker.
 *
 * <p>Preserves the manual-commit semantics of the previous Quarkus worker:
 * consumer auto-commit is off and containers acknowledge manually.
 */
@Configuration
public class KafkaConfig {

    private final String bootstrapServers;

    public KafkaConfig(@Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    @Bean
    public ProducerFactory<String, String> emailProducerFactory() {
        Map<String, Object> config = KafkaSecurityConfig.producer(
                bootstrapServers, KafkaSecurityConfig.DEFAULT_ACKS, true);
        config.put(ProducerConfig.RETRIES_CONFIG, 10);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> emailProducerFactory) {
        return new KafkaTemplate<>(emailProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, String> emailConsumerFactory(EmailProperties properties) {
        Map<String, Object> config = KafkaSecurityConfig.consumer(bootstrapServers, properties.getGroupId());
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /** Manual-ack container factory used by the main email listener. */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> manualAckListenerContainerFactory(
            ConsumerFactory<String, String> emailConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(emailConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    /** Retry listener uses its own group id. */
    @Bean
    public ConsumerFactory<String, String> retryConsumerFactory(EmailProperties properties) {
        Map<String, Object> config = new HashMap<>(
                KafkaSecurityConfig.consumer(bootstrapServers, properties.getRetryGroupId()));
        config.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getRetryGroupId());
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> retryListenerContainerFactory(
            ConsumerFactory<String, String> retryConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(retryConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
