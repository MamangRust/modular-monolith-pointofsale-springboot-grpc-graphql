package com.sanedge.pointofsale.gateway.dto.inputs;

public record RegisterInput(
        String firstname,
        String lastname,
        String username,
        String email,
        String password,
        String confirmPassword) {
}
