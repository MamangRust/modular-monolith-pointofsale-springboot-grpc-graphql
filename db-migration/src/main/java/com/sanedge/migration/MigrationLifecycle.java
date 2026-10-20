package com.sanedge.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Exits the JVM after Flyway has finished migrating.
 *
 * <p>Replaces the Quarkus {@code StartupEvent} / {@code Quarkus.asyncExit()}
 * pair: Spring Boot applies the migrations while the context is being refreshed,
 * so by the time this runner fires the schema is already up to date. Exiting
 * here turns the process into the run-to-completion Job the Kubernetes manifest
 * expects. The exit code propagates Flyway failures so the Job's
 * {@code restartPolicy: OnFailure} can retry.
 */
@Component
public class MigrationLifecycle implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationLifecycle.class);

    private final ConfigurableApplicationContext context;

    public MigrationLifecycle(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Migrations completed successfully. Shutting down migration runner.");
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }
}
