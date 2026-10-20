package com.sanedge.pointofsale;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.sanedge.pointofsale.product.ProductApplication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Module-local integration test base for the product service. The product
 * module owns no merchant entity, so {@code adminMerchant} is a lightweight
 * fixture exposing the merchant id used as the {@code merchant_id} column value.
 */
@SpringBootTest(classes = ProductApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public abstract class BaseIntegrationTest {

    @PersistenceContext
    protected EntityManager entityManager;

    protected final AdminMerchant adminMerchant = new AdminMerchant(1L);

    public static class AdminMerchant {
        private final Long merchantId;

        public AdminMerchant(Long merchantId) {
            this.merchantId = merchantId;
        }

        public Long getMerchantId() {
            return merchantId;
        }
    }
}
