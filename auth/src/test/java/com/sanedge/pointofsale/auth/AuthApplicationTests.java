package com.sanedge.pointofsale.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthApplicationTests {

    @Test
    void contextLoads() {
        // fails if any auth bean (handler, repositories, gRPC client config) cannot be wired
    }
}
