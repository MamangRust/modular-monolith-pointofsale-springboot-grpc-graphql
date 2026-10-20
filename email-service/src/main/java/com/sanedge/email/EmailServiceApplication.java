package com.sanedge.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Kafka-driven email worker. Boot entry point (replaces the previous Quarkus
 * {@code @QuarkusMain} / Arc bootstrap).
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class EmailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailServiceApplication.class, args);
    }
}
