package com.sanedge.pointofsale.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserApplicationTests {

    @Test
    void contextLoads() {
        // fails if any user bean (handlers, repositories, security, gRPC client config) cannot be wired
    }
}
