package com.sanedge.pointofsale.user.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.user.domain.requests.FindAllUserRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.domain.responses.api.PaginationMeta;
import com.sanedge.pointofsale.user.domain.responses.UserResponse;
import com.sanedge.pointofsale.user.domain.responses.UserResponseDeleteAt;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.user.models.User;
import com.sanedge.pointofsale.user.repository.UserQueryRepository;
import com.sanedge.pointofsale.user.service.UserQueryService;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserQueryImplService implements UserQueryService {
        private final UserQueryRepository userQueryRepository;
        private final TracingMetrics tracingMetrics;

        @Override
        public ApiResponsePagination<List<UserResponse>> findAll(FindAllUserRequest req) {
                return tracingMetrics.traceAndMeasure("findAllUsers", "find_all_users", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Searching all users | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        Pageable pageable = PageRequest.of(page, pageSize);
                        Page<User> userPage = userQueryRepository.findAllUsers(keyword, pageable);

                        List<UserResponse> userResponses = userPage.getContent()
                                        .stream()
                                        .map(UserResponse::from)
                                        .toList();

                        log.info("✅ Found {} users", userResponses.size());

                        return ApiResponsePagination.<List<UserResponse>>builder()
                                        .status("success")
                                        .message("Users retrieved successfully")
                                        .data(userResponses)
                                        .pagination(PaginationMeta.fromSpringPage(userPage))
                                        .build();
                });
        }

        @Override
        public ApiResponse<UserResponse> findById(Integer userId) {
                Attributes attrs = Attributes.builder()
                                .put("user.id", String.valueOf(userId))
                                .build();

                return tracingMetrics.traceAndMeasure("findUserById", "find_user_by_id", attrs, () -> {
                        log.info("🔍 Fetching user by id={}", userId);

                        User user = userQueryRepository.findByUserId(userId)
                                        .orElseThrow(() -> {
                                                log.error("❌ User not found | id={}", userId);
                                                return new ResourceNotFoundException("User not found");
                                        });

                        return ApiResponse.<UserResponse>builder()
                                        .status("success")
                                        .message("User retrieved successfully")
                                        .data(UserResponse.from(user))
                                        .build();
                });
        }

        @Override
        public ApiResponse<UserResponse> findByEmail(String email) {
                Attributes attrs = Attributes.builder()
                                .put("user.email", email != null ? email : "")
                                .build();

                return tracingMetrics.traceAndMeasure("findUserByEmail", "find_user_by_email", attrs, () -> {
                        log.info("🔍 Fetching user by email={}", email);

                        User user = userQueryRepository.findByEmail(email)
                                        .orElseThrow(() -> {
                                                log.error("❌ User not found | email={}", email);
                                                return new ResourceNotFoundException("User not found");
                                        });

                        return ApiResponse.<UserResponse>builder()
                                        .status("success")
                                        .message("User retrieved successfully")
                                        .data(UserResponse.from(user))
                                        .build();
                });
        }

        @Override
        public ApiResponse<UserResponse> findByUsername(String username) {
                Attributes attrs = Attributes.builder()
                                .put("user.username", username != null ? username : "")
                                .build();

                return tracingMetrics.traceAndMeasure("findUserByUsername", "find_user_by_username", attrs, () -> {
                        log.info("🔍 Fetching user by username={}", username);

                        User user = userQueryRepository.findByUsername(username)
                                        .orElseThrow(() -> {
                                                log.error("❌ User not found | username={}", username);
                                                return new ResourceNotFoundException("User not found");
                                        });

                        return ApiResponse.<UserResponse>builder()
                                        .status("success")
                                        .message("User retrieved successfully")
                                        .data(UserResponse.from(user))
                                        .build();
                });
        }

        @Override
        public ApiResponsePagination<List<UserResponseDeleteAt>> findByActive(FindAllUserRequest req) {
                return tracingMetrics.traceAndMeasure("findActiveUsers", "find_active_users", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🔍 Fetching active users | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        Pageable pageable = PageRequest.of(page, pageSize);
                        Page<User> userPage = userQueryRepository.findActiveUsers(keyword, pageable);

                        List<UserResponseDeleteAt> userResponses = userPage.getContent()
                                        .stream()
                                        .map(UserResponseDeleteAt::from)
                                        .toList();

                        log.info("✅ Found {} active users", userResponses.size());

                        return ApiResponsePagination.<List<UserResponseDeleteAt>>builder()
                                        .status("success")
                                        .message("Active users retrieved successfully")
                                        .data(userResponses)
                                        .pagination(PaginationMeta.fromSpringPage(userPage))
                                        .build();
                });
        }

        @Override
        public ApiResponsePagination<List<UserResponseDeleteAt>> findByTrashed(FindAllUserRequest req) {
                return tracingMetrics.traceAndMeasure("findTrashedUsers", "find_trashed_users", () -> {
                        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                        log.info("🗑️ Fetching trashed users | Page: {}, Size: {}, Search: {}", page + 1, pageSize,
                                        keyword.isEmpty() ? "None" : keyword);

                        Pageable pageable = PageRequest.of(page, pageSize);
                        Page<User> userPage = userQueryRepository.findTrashedUsers(keyword, pageable);

                        List<UserResponseDeleteAt> userResponses = userPage.getContent()
                                        .stream()
                                        .map(UserResponseDeleteAt::from)
                                        .toList();

                        log.info("🗑️ Found {} trashed users", userResponses.size());

                        return ApiResponsePagination.<List<UserResponseDeleteAt>>builder()
                                        .status("success")
                                        .message("Trashed users retrieved successfully")
                                        .data(userResponses)
                                        .pagination(PaginationMeta.fromSpringPage(userPage))
                                        .build();
                });
        }

}
