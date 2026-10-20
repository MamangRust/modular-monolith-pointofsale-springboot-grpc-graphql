package com.sanedge.email;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Spring Boot integration-test base: boots the full application context
 * against real Kafka and Redis containers (replacing the previous Quarkus dev
 * services / test resources).
 *
 * <p>Containers are static singletons so all integration tests share one
 * broker/cache within the Surefire JVM.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractKafkaRedisIT {

    @SuppressWarnings("resource")
    protected static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @SuppressWarnings("resource")
    protected static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379)
                    .waitingFor(Wait.forListeningPort());

    static {
        KAFKA.start();
        REDIS.start();
    }

    private static final boolean REDIS_AUTH = System.getenv("REDIS_PASSWORD") != null
            || !System.getProperty("redis.password", "").isEmpty();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        // The test Redis container has no auth; the app default password would
        // otherwise be sent as AUTH and the connection would fail.
        if (!REDIS_AUTH) {
            registry.add("spring.data.redis.password", () -> "");
        }
    }

    /** Bootstrap servers string for the shared broker (used by KafkaTestUtils). */
    protected static String bootstrapServers() {
        return KAFKA.getBootstrapServers();
    }
}
