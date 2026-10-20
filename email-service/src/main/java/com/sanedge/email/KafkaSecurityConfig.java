package com.sanedge.email;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka client config builder local to the email worker.
 *
 * <p>The email-service intentionally has <b>no</b> dependency on the
 * {@code common} module (which pulls in gRPC/panache beans), so the durability
 * and security settings are kept here in a single local source. The domain
 * services use the equivalent shared config.
 *
 * <p>Reads the same {@code KAFKA_*} env vars as the domain services:
 * {@code KAFKA_SECURITY_PROTOCOL}, {@code KAFKA_SASL_MECHANISM},
 * {@code KAFKA_SASL_JAAS_CONFIG}, {@code KAFKA_SSL_TRUSTSTORE_LOCATION/PASSWORD/TYPE},
 * {@code KAFKA_SSL_KEYSTORE_LOCATION/PASSWORD}, {@code KAFKA_SSL_KEY_PASSWORD}.
 * Plaintext (no env vars) stays the local-dev default.
 */
public final class KafkaSecurityConfig {

    public static final String DEFAULT_ACKS = "all";

    private KafkaSecurityConfig() {
    }

    /**
     * Producer config: String key/value serializers + {@code acks=all} +
     * {@code enable.idempotence=true} (env-overridable) + optional security.
     */
    public static Map<String, Object> producer(String bootstrapServers, String acks, boolean idempotence) {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", bootstrapServers);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("acks", acks);
        config.put("enable.idempotence", String.valueOf(idempotence));
        applySecurity(config);
        return config;
    }

    /**
     * Consumer config: String key / String value deserializers + group id
     * + earliest offset reset + manual commits + optional security.
     *
     * <p>Spring Kafka deserializes the JSON payload into a String; the worker
     * parses it via Jackson.
     */
    public static Map<String, Object> consumer(String bootstrapServers, String groupId) {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", bootstrapServers);
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", groupId);
        config.put("auto.offset.reset", "earliest");
        config.put("enable.auto.commit", "false");
        applySecurity(config);
        return config;
    }

    /** Applies SASL/SCRAM + TLS keys when {@code KAFKA_SECURITY_PROTOCOL} is set. */
    public static void applySecurity(Map<String, Object> config) {
        applySecurity(config, System.getenv());
    }

    /** Overload with explicit env source (used by unit tests). */
    public static void applySecurity(Map<String, Object> config, Map<String, String> env) {
        String protocol = env(env, "KAFKA_SECURITY_PROTOCOL", "PLAINTEXT");
        if (protocol.isBlank() || "PLAINTEXT".equalsIgnoreCase(protocol)) {
            return;
        }
        config.put("security.protocol", protocol);
        if (protocol.toUpperCase().contains("SASL")) {
            config.put("sasl.mechanism", env(env, "KAFKA_SASL_MECHANISM", "SCRAM-SHA-256"));
            String jaas = env(env, "KAFKA_SASL_JAAS_CONFIG", "");
            if (!jaas.isBlank()) {
                config.put("sasl.jaas.config", jaas);
            }
        }
        if (protocol.toUpperCase().contains("SSL")) {
            putIfNotBlank(config, "ssl.truststore.location", env(env, "KAFKA_SSL_TRUSTSTORE_LOCATION", ""));
            putIfNotBlank(config, "ssl.truststore.password", env(env, "KAFKA_SSL_TRUSTSTORE_PASSWORD", ""));
            putIfNotBlank(config, "ssl.truststore.type", env(env, "KAFKA_SSL_TRUSTSTORE_TYPE", "JKS"));
            putIfNotBlank(config, "ssl.keystore.location", env(env, "KAFKA_SSL_KEYSTORE_LOCATION", ""));
            putIfNotBlank(config, "ssl.keystore.password", env(env, "KAFKA_SSL_KEYSTORE_PASSWORD", ""));
            putIfNotBlank(config, "ssl.key.password", env(env, "KAFKA_SSL_KEY_PASSWORD", ""));
        }
    }

    private static String env(Map<String, String> env, String name, String defaultValue) {
        String value = env.get(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static void putIfNotBlank(Map<String, Object> config, String key, String value) {
        if (value != null && !value.isBlank()) {
            config.put(key, value);
        }
    }
}
