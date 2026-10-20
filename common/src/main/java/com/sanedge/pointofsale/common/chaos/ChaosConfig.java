package com.sanedge.pointofsale.common.chaos;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Root of the {@code chaos.yaml} configuration.
 *
 * <p>Spring port of the Quarkus {@code ChaosConfig}; the field set is unchanged
 * so an existing {@code chaos.yaml} keeps working as-is.
 */
@Data
public class ChaosConfig {

    private String warmupDuration = "0s";
    private boolean enableDefaultIgnored = true;
    private List<ChaosPolicy> policies = new ArrayList<>();
}
