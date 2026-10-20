package com.sanedge.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Minimal fake SMTP server (220 greeting + EHLO acknowledgement) on a fixed
 * local port. Spring Boot replacement for the previous Quarkus test resource:
 * {@code smtp.health.enabled} is a static property here, so the port can also
 * be static and shared.
 */
public final class FakeSmtpServer implements AutoCloseable {

    /** Fixed port so integration-test properties can reference it statically. */
    public static final int SMTP_PORT = 25252;

    private final ServerSocket server;
    private final ExecutorService acceptor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(true);

    public FakeSmtpServer() {
        try {
            // A BindException fails the test loudly rather than silently testing
            // the wrong server.
            this.server = new ServerSocket(SMTP_PORT);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start fake SMTP server on port " + SMTP_PORT, e);
        }
        acceptor.submit(this::acceptLoop);
    }

    private void acceptLoop() {
        while (running.get()) {
            try (Socket socket = server.accept()) {
                serve(socket);
            } catch (IOException e) {
                // Connection closed (close()) or transient accept error -> keep
                // looping while the server is still marked running.
            }
        }
    }

    private void serve(Socket socket) throws IOException {
        socket.setSoTimeout(5000);
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
    }

    @Override
    public void close() {
        running.set(false);
        acceptor.shutdownNow();
        try {
            server.close();
        } catch (IOException ignored) {
            // closing best effort
        }
    }
}
