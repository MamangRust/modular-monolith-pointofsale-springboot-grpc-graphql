package com.sanedge.pointofsale.user.domain.responses;

import com.sanedge.pointofsale.user.models.User;

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

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getUserId().intValue())
                .username(user.getUsername())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null)
                .build();
    }

}