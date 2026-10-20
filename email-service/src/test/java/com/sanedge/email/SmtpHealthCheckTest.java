package com.sanedge.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class SmtpHealthCheckTest {

    private SmtpHealthCheck check(int port, long timeoutMs) {
        return new SmtpHealthCheck("127.0.0.1", port, timeoutMs);
    }

    @Test
    void health_up_whenSmtpGreetsAndAcceptsEhlo() throws Exception {
        ExecutorService serverThread = Executors.newSingleThreadExecutor();
        try (ServerSocket server = new ServerSocket(0)) {
            serverThread.submit(() -> {
                try (Socket socket = server.accept()) {
                    OutputStream out = socket.getOutputStream();
                    out.write("220 fake-smtp ESMTP ready\r\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    String line = reader.readLine();
                    if (line != null && line.startsWith("EHLO")) {
                        out.write("250-fake-smtp\r\n250 OK\r\n".getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    }
                } catch (Exception ignored) {
                    // test asserts on the health response; connection errors => DOWN
                }
            });

            Health response = check(server.getLocalPort(), 3000).health();

            assertThat(response.getStatus()).isEqualTo(Status.UP);
            assertThat(response.getDetails()).containsEntry("host", "127.0.0.1");
        } finally {
            serverThread.shutdownNow();
        }
    }

    @Test
    void health_down_whenConnectionRefused() throws Exception {
        int unusedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            unusedPort = socket.getLocalPort();
        } // socket closed: connection to this port is refused

        Health response = check(unusedPort, 2000).health();

        assertThat(response.getStatus()).isEqualTo(Status.DOWN);
        assertThat(response.getDetails()).containsKey("error");
    }

    @Test
    void health_down_whenServerSendsUnexpectedBanner() throws Exception {
        ExecutorService serverThread = Executors.newSingleThreadExecutor();
        try (ServerSocket server = new ServerSocket(0)) {
            serverThread.submit(() -> {
                try (Socket socket = server.accept()) {
                    socket.getOutputStream()
                            .write("421 Service not available\r\n".getBytes(StandardCharsets.UTF_8));
                    socket.getOutputStream().flush();
                } catch (Exception ignored) {
                }
            });

            Health response = check(server.getLocalPort(), 3000).health();

            assertThat(response.getStatus()).isEqualTo(Status.DOWN);
            assertThat(response.getDetails().get("error").toString()).contains("greeting");
        } finally {
            serverThread.shutdownNow();
            serverThread.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
