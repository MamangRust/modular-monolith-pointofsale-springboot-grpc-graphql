package com.sanedge.pointofsale.common.chaos;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Injects SQL faults for statements matched by an enabled {@code sql} policy.
 *
 * <p>Replaces the Quarkus Vert.x {@code ChaosSqlProxy} / {@code ChaosSqlPoolProducer}
 * pair. Those wrapped a Vert.x {@code Pool} in a dynamic proxy — an approach that
 * has no Spring/JPA equivalent, because Hibernate owns the JDBC connection and
 * does not expose a per-query interceptor hook.
 *
 * <p>Hibernate's {@link StatementInspector} is the supported extension point: it
 * is invoked with the SQL text before every statement, and its return value is
 * the SQL that actually runs. That lets us:
 * <ul>
 *   <li>apply {@code latencyMs} by sleeping on the calling thread, and</li>
 *   <li>fail the statement by rewriting it into deliberately invalid SQL, which
 *       makes Hibernate surface the error through the normal
 *       {@code PersistenceException} path — mirroring the proxy's
 *       {@code Future.failedFuture(...)}.</li>
 * </ul>
 *
 * <p>Register it with {@code spring.jpa.properties.hibernate.session_factory.statement_inspector}
 * (see the JPA modules' application.properties).
 */
public class ChaosStatementInspector implements StatementInspector {

    private static final Logger log = LoggerFactory.getLogger(ChaosStatementInspector.class);

    /** Invalid SQL that reliably fails, carrying the configured reason. */
    private static final String FAILURE_SQL = "SELECT * FROM __chaos_injected_failure__";

    private final ChaosManager chaosManager;

    public ChaosStatementInspector(ChaosManager chaosManager) {
        this.chaosManager = chaosManager;
    }

    @Override
    public String inspect(String sql) {
        if (sql == null || chaosManager == null) {
            return sql;
        }

        ChaosPolicy policy = chaosManager.evaluate("sql", sql);
        if (policy == null) {
            policy = findTableMatch(sql);
        }

        if (policy == null || !policy.isEnabled() || Math.random() >= policy.getErrorChance()) {
            return sql;
        }

        log.info("Injecting SQL chaos [Policy: {}] for query: {}",
                policy.getName(), sql.trim().replaceAll("\\s+", " "));

        if (policy.getLatencyMs() > 0) {
            try {
                Thread.sleep(policy.getLatencyMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        boolean shouldFail = policy.getErrorMessage() != null || policy.getErrorCode() != 0;
        if (shouldFail) {
            log.warn("Failing SQL statement via chaos policy [{}]",
                    policy.getErrorMessage() != null ? policy.getErrorMessage() : "simulated SQL error");
            return FAILURE_SQL;
        }

        return sql;
    }

    /** Falls back to a substring match against the policy target (e.g. a table name). */
    private ChaosPolicy findTableMatch(String sql) {
        String lowerSql = sql.toLowerCase();
        for (ChaosPolicy policy : chaosManager.getPolicies()) {
            if (policy.isEnabled() && "sql".equalsIgnoreCase(policy.getType())) {
                String target = policy.getTarget();
                if (target != null && lowerSql.contains(target.toLowerCase())) {
                    return policy;
                }
            }
        }
        return null;
    }
}
