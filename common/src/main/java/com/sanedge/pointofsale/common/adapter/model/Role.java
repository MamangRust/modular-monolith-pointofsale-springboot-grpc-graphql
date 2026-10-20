package com.sanedge.pointofsale.common.adapter.model;

import java.time.Instant;

/**
 * Domain view of a role owned by the role service.
 */
public record Role(
        int id,
        String name,
        Instant createdAt,
        Instant updatedAt) {
}
