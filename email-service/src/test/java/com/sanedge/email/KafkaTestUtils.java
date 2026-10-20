package com.sanedge.email;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Minimal Kafka helpers for the email-service integration tests: produce a
 * record, await a matching record on another topic, and read committed offsets.
 * Uses the plain Kafka clients so tests stay independent of the app context.
 */
final class KafkaTestUtils {

    private KafkaTestUtils() {
    }

    /** Produces one record and waits for the broker to acknowledge it. */
    static void produce(String bootstrapServers, String topic, String key, String value) {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps(bootstrapServers))) {
            producer.send(new ProducerRecord<>(topic, null, key, value)).get();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while producing test record", interrupted);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new IllegalStateException("Failed to produce test record", e.getCause());
        }
    }

    /**
     * Consumes {@code topic} from the beginning with a throwaway group until a
     * record matches {@code matcher} or {@code timeout} elapses.
     */
    static ConsumerRecord<String, String> awaitRecord(String bootstrapServers, String topic,
            Predicate<ConsumerRecord<String, String>> matcher, Duration timeout) {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps(bootstrapServers))) {
            consumer.subscribe(Collections.singletonList(topic));
            long deadline = System.currentTimeMillis() + timeout.toMillis();
            while (System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    if (matcher.test(record)) {
                        return record;
                    }
                }
            }
            return null;
        }
    }

    /** Returns the highest committed offset across partitions for {@code group}/{@code topic}. */
    static long committedOffset(String bootstrapServers, String groupId, String topic, Duration timeout) {
        Properties props = new Properties();
        props.put(org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (org.apache.kafka.clients.admin.AdminClient admin =
                org.apache.kafka.clients.admin.AdminClient.create(props)) {
            long deadline = System.currentTimeMillis() + timeout.toMillis();
            while (System.currentTimeMillis() < deadline) {
                try {
                    java.util.Map<org.apache.kafka.common.TopicPartition,
                            org.apache.kafka.clients.consumer.OffsetAndMetadata> offsets =
                            admin.listConsumerGroupOffsets(groupId)
                                    .partitionsToOffsetAndMetadata()
                                    .get(5, java.util.concurrent.TimeUnit.SECONDS);
                    long highest = -1L;
                    for (java.util.Map.Entry<org.apache.kafka.common.TopicPartition,
                            org.apache.kafka.clients.consumer.OffsetAndMetadata> entry : offsets.entrySet()) {
                        if (entry.getKey().topic().equals(topic)
                                && entry.getValue() != null
                                && entry.getValue().offset() > highest) {
                            highest = entry.getValue().offset();
                        }
                    }
                    if (highest > 0) {
                        return highest;
                    }
                } catch (Exception ignored) {
                    // group may not have committed yet or is rebalancing -> retry
                }
                sleep(500);
            }
            return 0L;
        }
    }

    /** Record header helper for record-building call sites. */
    static RecordHeader header(String key, String value) {
        return new RecordHeader(key, value.getBytes(StandardCharsets.UTF_8));
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private static Properties producerProps(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return props;
    }

    private static Properties consumerProps(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "it-observer-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return props;
    }
}
