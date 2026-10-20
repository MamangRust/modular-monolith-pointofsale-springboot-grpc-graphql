package com.sanedge.pointofsale.common.chaos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Loads {@code chaos.yaml} and answers "which policy applies to this call?".
 *
 * <p>Spring port of the Quarkus {@code ChaosManager}. Notable differences:
 * <ul>
 *   <li>The Vert.x {@code setPeriodic} file watcher is replaced by a Spring
 *       {@link TaskScheduler} scheduled task (no Vert.x dependency).</li>
 *   <li>CDI's {@code @ApplicationScoped} / {@code @Startup} become
 *       {@code @Component}; {@code @PostConstruct}/{@code @PreDestroy} are the
 *       same Jakarta annotations.</li>
 * </ul>
 *
 * <p>Always registered — the manager is inert (empty policy list) when no
 * {@code chaos.yaml} is present, so it costs nothing in a normal deployment.
 */
@Component
public class ChaosManager {

    private static final Logger log = LoggerFactory.getLogger(ChaosManager.class);

    private static final String DEFAULT_CONFIG_PATH = "chaos.yaml";
    private static final long WATCH_INTERVAL_MS = 5_000L;

    private final TaskScheduler watcher;

    /** Overridden by the {@code CHAOS_CONFIG_PATH} env var. */
    @Value("${chaos.config-path:chaos.yaml}")
    private String configPath;

    private volatile ChaosConfig currentConfig = new ChaosConfig();
    private volatile long lastModified = 0;

    public ChaosManager() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("chaos-config-watcher-");
        scheduler.setDaemon(true);
        scheduler.initialize();
        this.watcher = scheduler;
    }

    @PostConstruct
    public void init() {
        String envPath = System.getenv("CHAOS_CONFIG_PATH");
        if (envPath != null && !envPath.isBlank()) {
            this.configPath = envPath;
        }
        loadConfig();
        startWatcher();
    }

    private void startWatcher() {
        watcher.scheduleWithFixedDelay(() -> {
            File file = new File(configPath);
            if (file.exists() && file.lastModified() > lastModified) {
                log.info("Detected modifications in {}, reloading chaos config...", configPath);
                loadConfig();
            }
        }, WATCH_INTERVAL_MS);
    }

    @PreDestroy
    public void destroy() {
        halt();
        if (watcher instanceof ThreadPoolTaskScheduler tpts) {
            tpts.shutdown();
        }
    }

    public synchronized void loadConfig() {
        ChaosResourceSabotage.haltAll();
        File file = new File(configPath);
        if (!file.exists()) {
            log.warn("Chaos config file not found at: {}. Using default empty config.", file.getAbsolutePath());
            this.currentConfig = new ChaosConfig();
            return;
        }

        try (InputStream input = new FileInputStream(file)) {
            LoaderOptions loaderOptions = new LoaderOptions();
            Yaml yaml = new Yaml(new Constructor(ChaosConfig.class, loaderOptions));
            ChaosConfig config = yaml.load(input);
            if (config == null) {
                config = new ChaosConfig();
            }
            this.currentConfig = config;
            this.lastModified = file.lastModified();
            log.info("Chaos configuration successfully loaded/reloaded from {}. Total policies: {}",
                    configPath, config.getPolicies().size());

            // Trigger resource sabotage if any policy is enabled
            for (ChaosPolicy policy : config.getPolicies()) {
                if (policy.isEnabled()) {
                    if ("cpu".equalsIgnoreCase(policy.getType())) {
                        ChaosResourceSabotage.startCpuPressure(policy.getCpuCores(), policy.getDuration());
                    } else if ("memory".equalsIgnoreCase(policy.getType())) {
                        ChaosResourceSabotage.startMemoryLeak(policy.getMemoryMb(), policy.getDuration());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load/parse chaos configuration from {}", configPath, e);
        }
    }

    public ChaosConfig getConfig() {
        return currentConfig;
    }

    public List<ChaosPolicy> getPolicies() {
        return currentConfig != null ? currentConfig.getPolicies() : new ArrayList<>();
    }

    /** Returns the first enabled policy matching {@code type} and {@code target}, or null. */
    public ChaosPolicy evaluate(String type, String target) {
        ChaosConfig cfg = currentConfig;
        if (cfg == null || cfg.getPolicies() == null) {
            return null;
        }

        for (ChaosPolicy policy : cfg.getPolicies()) {
            if (policy.isEnabled() && type.equalsIgnoreCase(policy.getType())) {
                if (matches(target, policy.getTarget())) {
                    return policy;
                }
            }
        }
        return null;
    }

    private boolean matches(String target, String pattern) {
        if (pattern == null) {
            return false;
        }
        if ("all".equalsIgnoreCase(pattern) || "*".equals(pattern)) {
            return true;
        }
        if (target == null) {
            return false;
        }

        if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return target.startsWith(prefix);
        }

        try {
            return target.equals(pattern) || target.matches(pattern);
        } catch (Exception e) {
            return target.contains(pattern);
        }
    }

    /** Disables every policy and stops ongoing resource sabotage. */
    public void halt() {
        log.info("Halt command triggered: disabling all chaos policies.");
        ChaosConfig cfg = currentConfig;
        if (cfg != null && cfg.getPolicies() != null) {
            for (ChaosPolicy policy : cfg.getPolicies()) {
                policy.setEnabled(false);
            }
        }
        ChaosResourceSabotage.haltAll();
    }
}
