package com.sanedge.pointofsale.common.adapter.model;

/**
 * Command to create a user in the user service.
 */
public record CreateUserCmd(
        String username,
        String firstname,
        String lastname,
        String email,
        String password,
        String confirmPassword) {
}
