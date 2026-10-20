package com.sanedge.pointofsale.common.adapter.model;

/**
 * Command to update a user in the user service.
 */
public record UpdateUserCmd(
        int id,
        String username,
        String firstname,
        String lastname,
        String email,
        String password,
        String confirmPassword) {
}
