package com.sanedge.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Flyway migration runner.
 *
 * <p>Spring Boot runs Flyway automatically on startup (see
 * {@code spring.flyway.enabled} / {@code spring.flyway.locations} in
 * application.properties). Once the context is up the migrations have already
 * been applied, so this is a run-to-completion process rather than a server —
 * {@link MigrationLifecycle} exits the JVM afterwards.
 *
 * <p>Runs as the Kubernetes {@code migrate} Job, which must complete before the
 * application workloads roll out (see the workload-wave patch in the production
 * overlay).
 */
@SpringBootApplication
public class DbMigrationApplication {

    public static void main(String[] args) {
        // web application type is NONE (see application.properties), so the
        // context starts, Flyway migrates, and MigrationLifecycle exits.
        SpringApplication.run(DbMigrationApplication.class, args);
    }
}
