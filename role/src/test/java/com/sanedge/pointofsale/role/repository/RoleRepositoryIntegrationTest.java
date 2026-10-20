package com.sanedge.pointofsale.role.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.role.models.Role;

public class RoleRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoleQueryRepository queryRepository;

    @Autowired
    private RoleCommandRepository commandRepository;

    @Test
    void shouldCreateAndQueryRole() {
        Role role = new Role();
        role.setRoleName("ROLE_MANAGER_UNIQUE");

        Role saved = commandRepository.save(role);
        assertThat(saved.getRoleId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        Page<Role> page = queryRepository.findRoles("ROLE_MANAGER_UNIQUE", PageRequest.of(0, 10));
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).getRoleName()).isEqualTo("ROLE_MANAGER_UNIQUE");

        Optional<Role> found = queryRepository.findByRoleName("ROLE_MANAGER_UNIQUE");
        assertThat(found).isPresent();
    }
}
