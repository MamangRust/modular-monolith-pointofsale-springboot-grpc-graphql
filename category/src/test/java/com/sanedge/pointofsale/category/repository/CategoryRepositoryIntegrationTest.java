package com.sanedge.pointofsale.category.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.category.models.Category;

public class CategoryRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CategoryQueryRepository categoryQueryRepository;

    @Autowired
    private CategoryCommandRepository categoryCommandRepository;

    @Test
    void shouldCreateAndQueryCategory() {
        Category category = new Category();
        category.setName("Home Appliances");
        category.setSlugCategory("home-appliances");
        category.setDescription("Appliances for home");
        
        Category saved = categoryCommandRepository.save(category);
        assertThat(saved.getCategoryId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        Page<Category> page = categoryQueryRepository.findCategories("Appliances", PageRequest.of(0, 10));
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).getName()).isEqualTo("Home Appliances");

        Optional<Category> found = categoryQueryRepository.findCategoryById(saved.getCategoryId());
        assertThat(found).isPresent();

        categoryCommandRepository.trashed(saved.getCategoryId());
        entityManager.flush();
        entityManager.clear();

        assertThat(categoryQueryRepository.findCategoryById(saved.getCategoryId())).isEmpty();
    }
}
