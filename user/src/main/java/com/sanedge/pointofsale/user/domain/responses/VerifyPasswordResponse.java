package com.sanedge.pointofsale.user.domain.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPasswordResponse {
    private boolean valid;
    private Integer userId;
    private String username;
    private String message;
}
