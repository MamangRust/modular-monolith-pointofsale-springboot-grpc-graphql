package com.sanedge.pointofsale.user.config;

import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sanedge.pointofsale.pb.role.ApiResponseRole;
import com.sanedge.pointofsale.pb.role.FindByNameRoleRequest;
import com.sanedge.pointofsale.pb.role.RoleQueryServiceGrpc;
import com.sanedge.pointofsale.user.models.Role;
import com.sanedge.pointofsale.user.models.User;
import com.sanedge.pointofsale.user.repository.UserCommandRepository;
import com.sanedge.pointofsale.user.repository.UserQueryRepository;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private final UserQueryRepository userQueryRepository;
    private final UserCommandRepository userCommandRepository;
    private final RoleQueryServiceGrpc.RoleQueryServiceBlockingStub roleQueryServiceBlockingStub;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userQueryRepository.count() > 0) {
            return;
        }

        Role adminRole = resolveRole("ROLE_ADMIN");
        Role userRole = resolveRole("ROLE_USER");

        if (adminRole != null) {
            userCommandRepository.save(buildUser("admin", "Admin", "User", "admin@sanedge.com", adminRole));
            log.info("Admin user seeded.");
        } else {
            log.warn("Skipping admin user seed: ROLE_ADMIN not found");
        }

        if (userRole != null) {
            userCommandRepository.save(buildUser("user", "John", "Doe", "user@sanedge.com", userRole));
            log.info("Default user seeded.");
        } else {
            log.warn("Skipping default user seed: ROLE_USER not found");
        }
    }

    private User buildUser(String username, String firstname, String lastname, String email, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRoles(Set.copyOf(List.of(role)));
        return user;
    }

    private Role resolveRole(String name) {
        try {
            ApiResponseRole res = roleQueryServiceBlockingStub.findByName(
                    FindByNameRoleRequest.newBuilder().setName(name).build());
            if (!res.hasData()) {
                return null;
            }
            Role role = new Role();
            role.setRoleId((long) res.getData().getRoleId());
            role.setRoleName(res.getData().getName());
            return role;
        } catch (StatusRuntimeException e) {
            log.warn("Skipping seed role {}: {}", name, e.getStatus().getCode());
            return null;
        }
    }
}
