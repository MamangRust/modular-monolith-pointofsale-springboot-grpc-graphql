package com.sanedge.pointofsale.gateway.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDeleteAt {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
}
