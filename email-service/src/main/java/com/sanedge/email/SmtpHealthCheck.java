package com.sanedge.email;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Dependency-aware readiness check for SMTP. Performs a real protocol-level
 * round trip (greeting + {@code EHLO}) against the configured mailer host, so
 * readiness reflects actual SMTP reachability rather than config presence.
 *
 * <p>Enabled only when {@code smtp.health.enabled=true} (mirrors the previous
 * Quarkus {@code @IfBuildProperty} guard).
 */
@Component("smtp")
@ConditionalOnProperty(name = "smtp.health.enabled", havingValue = "true")
public class SmtpHealthCheck implements HealthIndicator {

    private final String host;
    private final int port;
    private final long timeoutMs;

    public SmtpHealthCheck(
            @Value("${spring.mail.host:localhost}") String host,
            @Value("${spring.mail.port:587}") int port,
            @Value("${smtp.health.timeout-ms:3000}") long timeoutMs) {
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public Health health() {
        int effectiveTimeout = (int) Math.max(500, timeoutMs);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), effectiveTimeout);
            socket.setSoTimeout(effectiveTimeout);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            String banner = reader.readLine();
            if (banner == null || !banner.startsWith("220")) {
                return down("SMTP server did not send a 220 greeting", banner);
            }

            // RFC 5321: EHLO must precede any other command.
            OutputStream out = socket.getOutputStream();
            out.write("EHLO email-health\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();

            String ehlo = reader.readLine();
            if (ehlo == null || !ehlo.startsWith("250")) {
                return down("SMTP EHLO was rejected", ehlo);
            }

            return Health.up()
                    .withDetail("host", host)
                    .withDetail("port", port)
                    .build();
        } catch (Exception e) {
            return down(e.getMessage(), null);
        }
    }

    private Health down(String error, String detail) {
        Health.Builder builder = Health.down()
                .withDetail("host", host)
                .withDetail("port", port)
                .withDetail("error", error == null ? "unknown" : error);
        if (detail != null) {
            builder.withDetail("detail", detail);
        }
        return builder.build();
    }
}
