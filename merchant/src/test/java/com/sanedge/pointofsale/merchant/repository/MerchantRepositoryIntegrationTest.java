package com.sanedge.pointofsale.merchant.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.common.enums.Status;
import com.sanedge.pointofsale.merchant.models.Merchant;

public class MerchantRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MerchantQueryRepository queryRepository;

    @Autowired
    private MerchantCommandRepository commandRepository;

    @Test
    void shouldCreateAndQueryMerchant() {
        Merchant merchant = new Merchant();
        merchant.setUserId(adminUser.getUserId());
        merchant.setName("UniqueMerchantTest");
        merchant.setDescription("Merchant for integration test");
        merchant.setAddress("Test Address");
        merchant.setContactEmail("test@merchant.com");
        merchant.setContactPhone("0812345678");
        merchant.setStatus(Status.PENDING);

        Merchant saved = commandRepository.save(merchant);
        assertThat(saved.getMerchantId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        Page<Merchant> page = queryRepository.findMerchants("UniqueMerchantTest", PageRequest.of(0, 10));
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).getName()).isEqualTo("UniqueMerchantTest");

        Optional<Merchant> found = queryRepository.findMerchantById(saved.getMerchantId());
        assertThat(found).isPresent();
    }
}
