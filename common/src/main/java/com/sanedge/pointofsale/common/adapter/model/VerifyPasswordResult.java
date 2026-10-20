package com.sanedge.pointofsale.common.adapter.model;

/**
 * Result of a verify-password call against the user service.
 */
public record VerifyPasswordResult(
        boolean valid,
        int userId,
        String username,
        String message) {
}
