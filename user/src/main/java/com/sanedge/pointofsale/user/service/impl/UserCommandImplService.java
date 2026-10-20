package com.sanedge.pointofsale.user.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.user.domain.requests.CreateUserRequest;
import com.sanedge.pointofsale.user.domain.requests.UpdateUserRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.user.domain.responses.UserResponse;
import com.sanedge.pointofsale.user.domain.responses.UserResponseDeleteAt;
import com.sanedge.pointofsale.user.domain.responses.VerifyPasswordResponse;
import com.sanedge.pointofsale.common.adapter.role.RoleQueryPort;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.user.models.Role;
import com.sanedge.pointofsale.user.models.User;
import com.sanedge.pointofsale.user.repository.UserCommandRepository;
import com.sanedge.pointofsale.user.repository.UserQueryRepository;
import com.sanedge.pointofsale.user.service.UserCommandService;

import io.opentelemetry.api.common.Attributes;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserCommandImplService implements UserCommandService {
    private final UserQueryRepository userQueryRepository;
    private final RoleQueryPort roleQueryPort;
    private final UserCommandRepository userCommandRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final TracingMetrics tracingMetrics;

    private static final String DEFAULT_ROLE = "ROLE_ADMIN";

    @Override
    public ApiResponse<UserResponse> create(CreateUserRequest req) {
        Attributes attrs = Attributes.builder()
                .put("user.username", req.getUsername() != null ? req.getUsername() : "")
                .put("user.email", req.getEmail() != null ? req.getEmail() : "")
                .build();

        return tracingMetrics.traceAndMeasure("createUser", "create_user", attrs, () -> {
            validateRequest(req);

            Optional<User> existingUser = userQueryRepository.findByEmail(req.getEmail());
            if (existingUser.isPresent()) {
                log.error("❌ [REGISTER] Email already taken | Email: {}", req.getEmail());
                throw new IllegalArgumentException("Email already registered");
            }

            Role role = resolveDefaultRole();

            User newUser = new User();
            newUser.setUsername(req.getUsername());
            newUser.setFirstname(req.getFirstname());
            newUser.setLastname(req.getLastname());
            newUser.setEmail(req.getEmail());
            newUser.setPassword(passwordEncoder.encode(req.getPassword()));
            newUser.setRoles(Set.of(role));

            try {
                newUser = userCommandRepository.save(newUser);
            } catch (Exception e) {
                log.error("❌ Failed to create user", e);
                throw new ResourceNotFoundException("Failed to create user", e);
            }

            UserResponse userResponse = UserResponse.from(newUser);

            return ApiResponse.<UserResponse>builder()
                    .status("success")
                    .message("User registered successfully")
                    .data(userResponse)
                    .build();
        });
    }

    @Override
    public ApiResponse<UserResponse> update(UpdateUserRequest req) {
        Attributes attrs = Attributes.builder()
                .put("user.id", req.getId() != null ? req.getId().toString() : "")
                .build();

        return tracingMetrics.traceAndMeasure("updateUser", "update_user", attrs, () -> {
            validateRequest(req);

            User user = userQueryRepository.findById(req.getId().longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + req.getId()));

            if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
                Optional<User> existingUser = userQueryRepository.findByEmail(req.getEmail());
                if (existingUser.isPresent()) {
                    log.error("❌ [UPDATE] Email already taken | Email: {}", req.getEmail());
                    throw new IllegalArgumentException("Email already registered");
                }
                user.setEmail(req.getEmail());
            }

            if (req.getUsername() != null)
                user.setUsername(req.getUsername());
            if (req.getFirstname() != null)
                user.setFirstname(req.getFirstname());
            if (req.getLastname() != null)
                user.setLastname(req.getLastname());

            if (req.getPassword() != null) {
                if (!req.getPassword().equals(req.getConfirmPassword())) {
                    log.error("❌ [UPDATE] Password and confirm password do not match for user ID {}", req.getId());
                    throw new IllegalArgumentException("Password and confirm password do not match");
                }
                user.setPassword(passwordEncoder.encode(req.getPassword()));
            }

            Role role = resolveDefaultRole();
            user.setRoles(Set.of(role));

            user.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

            User updatedUser = userCommandRepository.save(user);

            log.info("✅ User updated successfully | User ID: {}", updatedUser.getUserId());

            return ApiResponse.<UserResponse>builder()
                    .status("success")
                    .message("✅ User updated successfully!")
                    .data(UserResponse.from(updatedUser))
                    .build();
        });
    }

    @Override
    public ApiResponse<UserResponseDeleteAt> trashed(Integer userId) {
        Attributes attrs = Attributes.builder()
                .put("user.id", String.valueOf(userId))
                .build();

        return tracingMetrics.traceAndMeasure("trashUser", "trash_user", attrs, () -> {
            log.info("🗑️ Trashing user id={}", userId);

            try {
                User user = userCommandRepository.trashed(userId.longValue());
                return ApiResponse.<UserResponseDeleteAt>builder()
                        .status("success")
                        .message("🗑️ User trashed successfully!")
                        .data(UserResponseDeleteAt.from(user))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to trash user id={}", userId, e);
                throw new ResourceNotFoundException("Failed to trash user", e);
            }
        });
    }

    @Override
    public ApiResponse<UserResponseDeleteAt> restore(Integer userId) {
        Attributes attrs = Attributes.builder()
                .put("user.id", String.valueOf(userId))
                .build();

        return tracingMetrics.traceAndMeasure("restoreUser", "restore_user", attrs, () -> {
            log.info("♻️ Restoring user id={}", userId);

            try {
                User user = userCommandRepository.restore(userId.longValue());
                return ApiResponse.<UserResponseDeleteAt>builder()
                        .status("success")
                        .message("♻️ User restored successfully!")
                        .data(UserResponseDeleteAt.from(user))
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore user id={}", userId, e);
                throw new ResourceNotFoundException("Failed to restore user", e);
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deletePermanent(Integer userId) {
        Attributes attrs = Attributes.builder()
                .put("user.id", String.valueOf(userId))
                .build();

        return tracingMetrics.traceAndMeasure("deleteUserPermanent", "delete_user_permanent", attrs, () -> {
            log.info("🧨 Permanently deleting user id={}", userId);

            try {
                userCommandRepository.deletePermanent(userId.longValue());
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🧨 User permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to permanently delete user id={}", userId, e);
                throw new ResourceNotFoundException("Failed to permanently delete user", e);
            }
        });
    }

    @Override
    public ApiResponse<Boolean> restoreAll() {
        return tracingMetrics.traceAndMeasure("restoreAllTrashedUsers", "restore_all_trashed_users", () -> {
            log.info("🔄 Restoring ALL trashed users");

            try {
                userCommandRepository.restoreAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("🔄 All users restored successfully!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to restore all users", e);
                throw new ResourceNotFoundException("Failed to restore all users", e);
            }
        });
    }

    @Override
    public ApiResponse<Boolean> deleteAll() {
        return tracingMetrics.traceAndMeasure("deleteAllTrashedUsers", "delete_all_trashed_users", () -> {
            log.info("💣 Permanently deleting ALL trashed users");

            try {
                userCommandRepository.deleteAllDeleted();
                return ApiResponse.<Boolean>builder()
                        .status("success")
                        .message("💣 All users permanently deleted!")
                        .data(true)
                        .build();
            } catch (Exception e) {
                log.error("💥 Failed to delete all users", e);
                throw new ResourceNotFoundException("Failed to delete all users", e);
            }
        });
    }

    @Override
    public VerifyPasswordResponse verifyPassword(String username, String password) {
        Attributes attrs = Attributes.builder()
                .put("user.username", username != null ? username : "")
                .build();

        return tracingMetrics.traceAndMeasure("verifyPassword", "verify_password", attrs, () -> {
            Optional<User> userOpt = userQueryRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return VerifyPasswordResponse.builder()
                        .valid(false)
                        .username(username)
                        .message("User not found")
                        .build();
            }

            User user = userOpt.get();
            boolean valid = passwordEncoder.matches(password, user.getPassword());

            return VerifyPasswordResponse.builder()
                    .valid(valid)
                    .userId(user.getUserId().intValue())
                    .username(user.getUsername())
                    .message(valid ? "Password verified" : "Invalid password")
                    .build();
        });
    }

    private Role resolveDefaultRole() {
        com.sanedge.pointofsale.common.adapter.model.Role dr = roleQueryPort.findByName(DEFAULT_ROLE);
        Role role = new Role();
        role.setRoleId((long) dr.id());
        role.setRoleName(dr.name());
        return role;
    }

    private <T> void validateRequest(T req) {
        Set<ConstraintViolation<T>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
            }
            log.error("Validation failed: {}", sb.toString());
            throw new ConstraintViolationException("Validation failed: " + sb, violations);
        }
    }

}
