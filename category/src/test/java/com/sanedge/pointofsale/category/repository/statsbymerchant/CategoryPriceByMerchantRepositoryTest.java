package com.sanedge.pointofsale.category.repository.statsbymerchant;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.category.models.Category;
import com.sanedge.pointofsale.category.models.CategoryMonthPrice;
import com.sanedge.pointofsale.category.models.CategoryYearPrice;
import com.sanedge.pointofsale.category.repository.CategoryCommandRepository;

public class CategoryPriceByMerchantRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private CategoryPriceByMerchantRepository categoryPriceByMerchantRepository;

    @Autowired
    private CategoryCommandRepository categoryRepository;

    private Long merchantId;

    @BeforeEach
    void setUp() {
        merchantId = 9001L;
        Long productId = 8001L;
        Long orderId = 7001L;

        Category category = new Category();
        category.setName("Test Category");
        category.setSlugCategory("test-category");
        category.setDescription("Test Category Description");
        category = categoryRepository.save(category);

        jdbcTemplate.update(
                "INSERT INTO products (product_id, merchant_id, category_id, name, slug_product, price, count_in_stock) VALUES (?,?,?,?,?,?,?)",
                productId, merchantId, category.getCategoryId(), "Test Product", "test-product", 100, 10);

        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 12, 0);
        jdbcTemplate.update(
                "INSERT INTO orders (order_id, cashier_id, merchant_id, total_price, created_at, updated_at) VALUES (?,?,?,?,?,?)",
                orderId, 1L, merchantId, 100L, Timestamp.valueOf(createdAt), Timestamp.valueOf(createdAt));

        jdbcTemplate.update(
                "INSERT INTO order_items (order_item_id, order_id, product_id, quantity, price, created_at, updated_at) VALUES (?,?,?,?,?,?,?)",
                6001L, orderId, productId, 1, 100, Timestamp.valueOf(createdAt), Timestamp.valueOf(createdAt));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findMonthlyCategoryStatsByMerchant_ShouldReturnCorrectStats() {
        List<CategoryMonthPrice> stats = categoryPriceByMerchantRepository.findMonthlyCategoryPriceByMerchant(
                merchantId, 2024);

        assertThat(stats).isNotEmpty();
        assertThat(stats.get(0).getMonth()).isEqualTo("Jan");
        assertThat(stats.get(0).getCategoryName()).isEqualTo("Test Category");
        assertThat(stats.get(0).getTotalRevenue()).isEqualTo(100L);
    }

    @Test
    void findYearlyCategoryStatsByMerchant_ShouldReturnCorrectStats() {
        List<CategoryYearPrice> stats = categoryPriceByMerchantRepository.findYearlyCategoryPriceByMerchant(
                merchantId, 2024);

        assertThat(stats).isNotEmpty();
        assertThat(stats.get(0).getYear()).isEqualTo("2024");
        assertThat(stats.get(0).getCategoryName()).isEqualTo("Test Category");
        assertThat(stats.get(0).getTotalRevenue()).isEqualTo(100L);
    }
}
