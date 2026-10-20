package com.sanedge.pointofsale.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.sanedge.pointofsale")
@EntityScan({ "com.sanedge.pointofsale.transaction.models", "com.sanedge.pointofsale.common.outbox" })
@EnableScheduling
public class TransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionApplication.class, args);
    }
}
