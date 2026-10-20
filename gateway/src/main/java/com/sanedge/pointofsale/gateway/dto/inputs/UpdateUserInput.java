package com.sanedge.pointofsale.gateway.dto.inputs;

public record UpdateUserInput(
        Integer id,
        String username,
        String firstname,
        String lastname,
        String email,
        String password,
        String confirmPassword) {
}
