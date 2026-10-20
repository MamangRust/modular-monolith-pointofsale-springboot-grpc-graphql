package com.sanedge.pointofsale.product.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.product.models.Product;

public class ProductRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductQueryRepository productQueryRepository;

    @Autowired
    private ProductCommandRepository productCommandRepository;

    @Test
    void shouldCreateAndQueryProduct() {
        Product product = new Product();
        product.setName("UniqueAdvancedProduct");
        product.setPrice(120000);
        product.setCategoryId(1L);
        product.setMerchantId(adminMerchant.getMerchantId());
        product.setCountInStock(5);
        product.setSlugProduct("unique-advanced-ai-gadget");
        
        Product saved = productCommandRepository.save(product);
        assertThat(saved.getProductId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        Page<Product> page = productQueryRepository.findAllProducts("UniqueAdvancedProduct", PageRequest.of(0, 10));
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).getName()).isEqualTo("UniqueAdvancedProduct");

        Optional<Product> found = productQueryRepository.findProductById(saved.getProductId());
        assertThat(found).isPresent();
    }
}
