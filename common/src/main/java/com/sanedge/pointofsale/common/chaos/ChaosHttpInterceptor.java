package com.sanedge.pointofsale.common.chaos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Injects HTTP faults (status code, body, latency, dropped connection) for
 * requests matched by an enabled {@code http} policy.
 *
 * <p>Spring port of the Quarkus Vert.x {@code ChaosHttpMiddleware}. Mapping:
 * <ul>
 *   <li>{@code RoutingContext} → {@link HttpServletRequest}/{@link HttpServletResponse}</li>
 *   <li>{@code ctx.next()} → return {@code true} from {@code preHandle}</li>
 *   <li>{@code ctx.vertx().setTimer(...)} → {@link Thread#sleep} (this is already
 *       a request-handling thread that is about to be blocked by design)</li>
 *   <li>abrupt connection drop → {@code response.reset()} + mark the request so
 *       the caller does not get a well-formed reply</li>
 * </ul>
 *
 * <p>Only active in web modules: gated on spring-webmvc being present and on
 * {@code chaos.http.enabled} defaulting to true.
 */
@Component
@ConditionalOnClass(HandlerInterceptor.class)
@ConditionalOnProperty(name = "chaos.http.enabled", havingValue = "true", matchIfMissing = true)
public class ChaosHttpInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ChaosHttpInterceptor.class);

    private final ChaosManager manager;

    public ChaosHttpInterceptor(ChaosManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Never sabotage health/metrics probes or the chaos control surface itself.
        if (path.equals("/health")
                || path.startsWith("/actuator")
                || path.startsWith("/q/health")
                || path.startsWith("/api/chaos")) {
            return true;
        }

        String fullTarget = method + ":" + path;
        ChaosPolicy policy = manager.evaluate("http", fullTarget);
        if (policy == null) {
            policy = manager.evaluate("http", path);
        }

        if (policy == null || !policy.isEnabled() || Math.random() >= policy.getErrorChance()) {
            return true;
        }

        log.info("Injecting HTTP chaos [Policy: {}] to request: {} {}", policy.getName(), method, path);

        if (policy.isDropConnection()) {
            log.warn("Dropping connection abruptly (drop_connection) for: {} {}", method, path);
            // Reset the response so no headers/body are committed, then abort the
            // request without writing a reply — the client sees a broken connection.
            response.reset();
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }

        if (policy.getLatencyMs() > 0) {
            try {
                Thread.sleep(policy.getLatencyMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        int code = policy.getErrorCode() != 0 ? policy.getErrorCode() : 503;
        String body = policy.getErrorBody() != null
                ? policy.getErrorBody()
                : "{\"error\":\"chaos_fault\",\"message\":\"Simulated chaos error\"}";

        response.setStatus(code);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(body);
        return false; // stop the chain: the fault response is the reply
    }
}
