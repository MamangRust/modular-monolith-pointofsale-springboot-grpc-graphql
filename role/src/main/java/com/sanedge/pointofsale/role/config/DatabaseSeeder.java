package com.sanedge.pointofsale.role.config;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sanedge.pointofsale.role.models.Role;
import com.sanedge.pointofsale.role.repository.RoleCommandRepository;
import com.sanedge.pointofsale.role.repository.RoleQueryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleQueryRepository roleQueryRepository;
    private final RoleCommandRepository roleCommandRepository;

    @Override
    public void run(String... args) {
        try {
            seedRoles();
            log.info("Database seeding completed successfully.");
        } catch (Exception e) {
            log.error("Error during database seeding: {}", e.getMessage());
        }
    }

    private void seedRoles() {
        if (roleQueryRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setRoleName("ROLE_ADMIN");

            Role userRole = new Role();
            userRole.setRoleName("ROLE_USER");

            roleCommandRepository.saveAll(Arrays.asList(adminRole, userRole));
            log.info("Roles seeded.");
        }
    }
}
