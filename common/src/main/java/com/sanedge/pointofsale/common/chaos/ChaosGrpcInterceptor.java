package com.sanedge.pointofsale.common.chaos;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import jakarta.annotation.PreDestroy;

/**
 * Injects gRPC faults (latency or a non-OK status) for methods matched by an
 * enabled {@code grpc} policy.
 *
 * <p>Spring port of the Quarkus {@code @GlobalInterceptor}: the Quarkus version
 * was auto-registered as a CDI global interceptor, whereas here it is a plain
 * {@code @Component} that {@code GrpcServerLifecycle} picks up and attaches to
 * the server it builds. The Vert.x {@code setTimer} delay is replaced by a small
 * daemon {@link ScheduledExecutorService}.
 */
@Component
@Order(-100)
public class ChaosGrpcInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ChaosGrpcInterceptor.class);

    private final ChaosManager chaosManager;
    private final ScheduledExecutorService scheduler;

    public ChaosGrpcInterceptor(ChaosManager chaosManager) {
        this.chaosManager = chaosManager;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "chaos-grpc-delay");
            t.setDaemon(true);
            return t;
        });
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // format: "PackageName.ServiceName/MethodName"
        String fullMethod = call.getMethodDescriptor().getFullMethodName();

        ChaosPolicy policy = chaosManager.evaluate("grpc", fullMethod);

        if (policy == null) {
            // fall back to a service-level wildcard match
            String serviceName = fullMethod.split("/")[0];
            policy = chaosManager.evaluate("grpc", serviceName + "/*");
        }

        if (policy != null && policy.isEnabled() && Math.random() < policy.getErrorChance()) {
            log.info("Injecting gRPC chaos [Policy: {}] for method: {}", policy.getName(), fullMethod);

            final ChaosPolicy finalPolicy = policy;

            if (policy.getLatencyMs() > 0) {
                long delay = policy.getLatencyMs();
                scheduler.schedule(() -> call.close(resolveGrpcStatus(finalPolicy), new Metadata()),
                        delay, TimeUnit.MILLISECONDS);
            } else {
                call.close(resolveGrpcStatus(finalPolicy), new Metadata());
            }
            return new Listener<>() {
            }; // no-op listener: the call is already closed
        }

        return next.startCall(call, headers);
    }

    private Status resolveGrpcStatus(ChaosPolicy policy) {
        String msg = policy.getErrorMessage() != null
                ? policy.getErrorMessage()
                : "Chaos fault injected";

        String grpcStatus = policy.getGrpcStatus();
        if (grpcStatus == null) {
            return Status.UNAVAILABLE.withDescription(msg);
        }

        return switch (grpcStatus.toUpperCase()) {
            case "UNAVAILABLE" -> Status.UNAVAILABLE.withDescription(msg);
            case "DEADLINE_EXCEEDED" -> Status.DEADLINE_EXCEEDED.withDescription(msg);
            case "INTERNAL" -> Status.INTERNAL.withDescription(msg);
            case "RESOURCE_EXHAUSTED" -> Status.RESOURCE_EXHAUSTED.withDescription(msg);
            case "NOT_FOUND" -> Status.NOT_FOUND.withDescription(msg);
            case "PERMISSION_DENIED" -> Status.PERMISSION_DENIED.withDescription(msg);
            default -> Status.UNKNOWN.withDescription(msg);
        };
    }
}
