package com.sanedge.pointofsale.common.grpc;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import lombok.extern.slf4j.Slf4j;

/**
 * Hosts every {@link BindableService} bean in the application context on a single
 * gRPC server. Only activated when {@code grpc.server.port} is configured, so
 * pure REST modules (gateway) do not open a gRPC port.
 */
@Slf4j
@Component
@ConditionalOnProperty("grpc.server.port")
public class GrpcServerLifecycle implements SmartLifecycle {

    private final List<BindableService> services;
    private final List<ServerInterceptor> interceptors;
    private final int port;
    private Server server;
    private volatile boolean running = false;

    public GrpcServerLifecycle(List<BindableService> services,
            ObjectProvider<ServerInterceptor> interceptors,
            @Value("${grpc.server.port}") int port) {
        this.services = services;
        // Ordered by @Order; empty in a normal deployment (no chaos interceptor).
        this.interceptors = interceptors.orderedStream().toList();
        this.port = port;
    }

    @Override
    public void start() {
        ServerBuilder<?> builder = ServerBuilder.forPort(port);
        services.forEach(builder::addService);
        // e.g. ChaosGrpcInterceptor when chaos engineering is enabled
        interceptors.forEach(builder::intercept);
        // enables grpcurl / server reflection for manual testing
        builder.addService(ProtoReflectionServiceV1.newInstance());

        this.server = builder.build();
        try {
            server.start();
            running = true;
            log.info("gRPC server started on port {} with services: {}", port,
                    services.stream().map(s -> s.getClass().getSimpleName()).toList());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start gRPC server on port " + port, e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
            try {
                if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1;
    }
}
