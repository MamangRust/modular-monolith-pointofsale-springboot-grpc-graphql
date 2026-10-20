package com.sanedge.pointofsale.common.chaos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single fault-injection policy from {@code chaos.yaml}.
 *
 * <p>Field names map 1:1 onto the YAML keys (SnakeYAML binds them by property
 * name), so the existing configuration file needs no changes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChaosPolicy {

    private String name;
    private String type;   // "http", "sql", "grpc", "cpu", "memory"
    private String target; // "all", "POST:/api/auth/login", "SELECT", etc.

    @Builder.Default
    private boolean enabled = false;

    @Builder.Default
    private double errorChance = 0.0; // 0.0 to 1.0

    private int errorCode;        // e.g. 503, 429
    private String errorMessage;  // e.g. "PgException"
    private String errorBody;     // e.g. json payload
    private long latencyMs;       // delay duration
    private boolean dropConnection;
    private boolean exhaustConnections;
    private int cpuCores;
    private int memoryMb;
    private String duration;      // e.g. "5m"
    private boolean dropMessage;  // kafka: silently drop
    private boolean rejectMessage; // kafka: fail with error

    private String grpcStatus;    // "UNAVAILABLE", "DEADLINE_EXCEEDED", "INTERNAL", etc.
    private boolean resetStream;  // abruptly close stream
    private long deadlineMs;      // simulate deadline exceeded
}
