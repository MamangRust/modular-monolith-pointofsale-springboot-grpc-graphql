package com.sanedge.pointofsale.auth.domain.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String createdAt;
    private String updatedAt;

    public static UserResponse from(com.sanedge.pointofsale.pb.user.UserResponse u) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .firstname(u.getFirstname())
                .lastname(u.getLastname())
                .email(u.getEmail())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    public static UserResponse from(com.sanedge.pointofsale.common.adapter.model.User u) {
        return UserResponse.builder()
                .id(u.id())
                .username(u.username())
                .firstname(u.firstname())
                .lastname(u.lastname())
                .email(u.email())
                .createdAt(u.createdAt() != null ? u.createdAt().toString() : null)
                .updatedAt(u.updatedAt() != null ? u.updatedAt().toString() : null)
                .build();
    }
}
