package com.sanedge.pointofsale.category.config;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sanedge.pointofsale.category.models.Category;
import com.sanedge.pointofsale.category.repository.CategoryCommandRepository;
import com.sanedge.pointofsale.category.repository.CategoryQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final CategoryQueryRepository categoryQueryRepository;
    private final CategoryCommandRepository categoryCommandRepository;

    @Override
    public void run(String... args) {
        try {
            seedCategories();
            log.info("Database seeding completed successfully.");
        } catch (Exception e) {
            log.error("Error during database seeding: {}", e.getMessage());
        }
    }

    private void seedCategories() {
        if (categoryQueryRepository.count() == 0) {
            Category electronics = new Category();
            electronics.setName("Electronics");
            electronics.setDescription("Electronic devices and gadgets");
            electronics.setSlugCategory("electronics");

            Category fashion = new Category();
            fashion.setName("Fashion");
            fashion.setDescription("Clothing and accessories");
            fashion.setSlugCategory("fashion");

            categoryCommandRepository.saveAll(Arrays.asList(electronics, fashion));
            log.info("Categories seeded.");
        }
    }
}
