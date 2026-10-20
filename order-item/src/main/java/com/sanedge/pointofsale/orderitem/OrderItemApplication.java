package com.sanedge.pointofsale.orderitem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.sanedge.pointofsale")
public class OrderItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderItemApplication.class, args);
    }
}
