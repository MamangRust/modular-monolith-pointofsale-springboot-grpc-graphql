package com.sanedge.pointofsale.cashier;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CashierApplicationTests {

    @Test
    void contextLoads() {
        // fails if any cashier bean (handlers, repositories, gRPC client config) cannot be wired
    }
}
