package com.sanedge.pointofsale;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.sanedge.pointofsale.merchant.MerchantApplication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Module-local integration test base for the merchant service. The merchant
 * module owns no user entity, so {@code adminUser} is a lightweight fixture
 * exposing the admin user id used as the {@code user_id} column value.
 */
@SpringBootTest(classes = MerchantApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public abstract class BaseIntegrationTest {

    @PersistenceContext
    protected EntityManager entityManager;

    protected final AdminUser adminUser = new AdminUser(1L);

    public static class AdminUser {
        private final Long userId;

        public AdminUser(Long userId) {
            this.userId = userId;
        }

        public Long getUserId() {
            return userId;
        }
    }
}
