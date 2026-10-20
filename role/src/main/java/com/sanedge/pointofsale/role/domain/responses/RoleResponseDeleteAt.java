package com.sanedge.pointofsale.role.domain.responses;

import com.sanedge.pointofsale.role.models.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDeleteAt {
    private Integer id;
    private String name;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public static RoleResponseDeleteAt from(Role role) {
        if (role == null) {
            return null;
        }
        return RoleResponseDeleteAt.builder()
                .id(role.getRoleId().intValue())
                .name(role.getRoleName())
                .createdAt(role.getCreatedAt() != null ? role.getCreatedAt().toString() : null)
                .updatedAt(role.getUpdatedAt() != null ? role.getUpdatedAt().toString() : null)
                .deletedAt(role.getDeletedAt() != null ? role.getDeletedAt().toString() : null)
                .build();
    }
}