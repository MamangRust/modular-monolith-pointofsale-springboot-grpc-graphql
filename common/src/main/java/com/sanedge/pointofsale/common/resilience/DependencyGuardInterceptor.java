package com.sanedge.pointofsale.common.resilience;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Deadline;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;

/**
 * Padanan Spring dari {@code DependencyGuardInterceptor} (Quarkus
 * {@code @GlobalInterceptor}). Membungkus setiap {@link ClientCall} gRPC dengan
 * circuit breaker + bulkhead + timeout per-method memakai resilience4j.
 *
 * <p>Interceptor didaftarkan pada channel lewat {@code .intercept(...)} di tiap
 * {@code GrpcClientConfig} (Spring tidak punya {@code @GlobalInterceptor}).
 */
@Component
public class DependencyGuardInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DependencyGuardInterceptor.class);

    private final DependencyGuardProperties config;

    private final ConcurrentMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Bulkhead> bulkheads = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler;

    public DependencyGuardInterceptor(DependencyGuardProperties config) {
        this.config = config;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "dependency-guard-timeout");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        String fullMethod = method.getFullMethodName();
        CircuitBreaker circuitBreaker = circuitBreakers.computeIfAbsent(fullMethod, this::buildCircuitBreaker);
        Bulkhead bulkhead = bulkheads.computeIfAbsent(fullMethod, this::buildBulkhead);
        return new GuardedClientCall<>(next.newCall(method, applyDeadline(callOptions)), circuitBreaker, bulkhead,
                config.getCallTimeout().toMillis(), scheduler);
    }

    CircuitBreaker buildCircuitBreaker(String fullMethod) {
        CircuitBreakerConfig cfg = CircuitBreakerConfig.custom()
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .slidingWindowSize((int) config.getFailureThreshold())
                .minimumNumberOfCalls((int) config.getFailureThreshold())
                .failureRateThreshold(50f)
                .waitDurationInOpenState(java.time.Duration.ofSeconds(config.getOpenTimeoutSeconds()))
                .permittedNumberOfCallsInHalfOpenState(1)
                // Hanya kegagalan transport yang dihitung sebagai failure;
                // error bisnis tidak membuka sirkuit.
                .recordException(DependencyGuardInterceptor::isTransportFailure)
                .build();
        return CircuitBreaker.of(fullMethod, cfg);
    }

    Bulkhead buildBulkhead(String fullMethod) {
        BulkheadConfig cfg = BulkheadConfig.custom()
                .maxConcurrentCalls(config.getMaxConcurrent())
                .maxWaitDuration(java.time.Duration.ZERO)
                .build();
        return Bulkhead.of(fullMethod, cfg);
    }

    /**
     * Memasang deadline per-call, tetapi tidak pernah memperpanjang deadline
     * yang sudah ada (mirip {@code context.WithTimeout} di Go: ambil yang lebih
     * pendek). Deadline ini yang membatalkan RPC di level transport saat
     * timeout.
     */
    private CallOptions applyDeadline(CallOptions callOptions) {
        long timeoutMs = config.getCallTimeout().toMillis();
        Deadline existing = callOptions.getDeadline();
        if (existing == null || existing.timeRemaining(TimeUnit.MILLISECONDS) > timeoutMs) {
            return callOptions.withDeadline(Deadline.after(timeoutMs, TimeUnit.MILLISECONDS));
        }
        return callOptions;
    }

    /** Hanya kegagalan transport yang dihitung sebagai failure circuit breaker. */
    static boolean isTransportFailure(Throwable error) {
        Throwable cause = unwrap(error);
        if (cause instanceof TimeoutException) {
            return true;
        }
        if (cause instanceof StatusRuntimeException sre) {
            return isTransportStatus(sre.getStatus());
        }
        if (cause instanceof StatusException se) {
            return isTransportStatus(se.getStatus());
        }
        return false;
    }

    private static boolean isTransportStatus(Status status) {
        return switch (status.getCode()) {
            case UNAVAILABLE, DEADLINE_EXCEEDED, ABORTED, RESOURCE_EXHAUSTED -> true;
            default -> false;
        };
    }

    /** Memetakan kegagalan guard/koneksi ke status gRPC yang setara. */
    static Status toStatus(Throwable error) {
        Throwable cause = unwrap(error);
        if (cause instanceof StatusRuntimeException sre) {
            return sre.getStatus();
        }
        if (cause instanceof StatusException se) {
            return se.getStatus();
        }
        if (cause instanceof TimeoutException) {
            return Status.DEADLINE_EXCEEDED.withDescription("dependency call timed out");
        }
        if (cause instanceof CallNotPermittedException) {
            return Status.UNAVAILABLE.withDescription("dependency circuit open; failing fast");
        }
        if (cause instanceof BulkheadFullException) {
            return Status.RESOURCE_EXHAUSTED.withDescription("dependency bulkhead full");
        }
        return Status.UNKNOWN.withDescription(cause.getClass().getSimpleName() + ": " + cause.getMessage());
    }

    private static Throwable unwrap(Throwable error) {
        Throwable current = error;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * Membungkus {@link ClientCall} agar eksekusi lewat circuit breaker +
     * bulkhead + timeout. Pesan dan header diteruskan apa adanya;
     * {@code onClose} baru diteruskan setelah call selesai (sukses, ditolak
     * bulkhead, sirkuit terbuka, atau timeout).
     */
    static final class GuardedClientCall<ReqT, RespT> extends ClientCall<ReqT, RespT> {

        private final ClientCall<ReqT, RespT> delegate;
        private final CircuitBreaker circuitBreaker;
        private final Bulkhead bulkhead;
        private final long timeoutMillis;
        private final ScheduledExecutorService scheduler;

        GuardedClientCall(ClientCall<ReqT, RespT> delegate, CircuitBreaker circuitBreaker, Bulkhead bulkhead,
                long timeoutMillis, ScheduledExecutorService scheduler) {
            this.delegate = delegate;
            this.circuitBreaker = circuitBreaker;
            this.bulkhead = bulkhead;
            this.timeoutMillis = timeoutMillis;
            this.scheduler = scheduler;
        }

        @Override
        public void start(Listener<RespT> listener, Metadata headers) {
            long startNanos = System.nanoTime();

            // Circuit breaker outermost: fail-fast tanpa menyentuh bulkhead.
            if (!circuitBreaker.tryAcquirePermission()) {
                listener.onClose(Status.UNAVAILABLE.withDescription("dependency circuit open; failing fast"),
                        new Metadata());
                return;
            }
            if (!bulkhead.tryAcquirePermission()) {
                circuitBreaker.releasePermission();
                listener.onClose(Status.RESOURCE_EXHAUSTED.withDescription("dependency bulkhead full"),
                        new Metadata());
                return;
            }

            AtomicReference<Metadata> trailers = new AtomicReference<>();
            CompletableFuture<RespT> future = new CompletableFuture<>();

            ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
                if (future.completeExceptionally(new TimeoutException("dependency call timed out"))) {
                    delegate.cancel("dependency call timed out", null);
                }
            }, timeoutMillis, TimeUnit.MILLISECONDS);

            try {
                delegate.start(new Listener<RespT>() {
                    @Override
                    public void onMessage(RespT message) {
                        listener.onMessage(message);
                    }

                    @Override
                    public void onHeaders(Metadata responseHeaders) {
                        listener.onHeaders(responseHeaders);
                    }

                    @Override
                    public void onReady() {
                        listener.onReady();
                    }

                    @Override
                    public void onClose(Status status, Metadata responseTrailers) {
                        trailers.set(responseTrailers);
                        if (status.isOk()) {
                            future.complete(null);
                        } else {
                            future.completeExceptionally(status.asRuntimeException(responseTrailers));
                        }
                    }
                }, headers);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }

            future.whenComplete((value, error) -> {
                timeoutTask.cancel(false);
                long durationNanos = System.nanoTime() - startNanos;
                bulkhead.onComplete();
                if (error == null) {
                    circuitBreaker.onSuccess(durationNanos, TimeUnit.NANOSECONDS);
                } else {
                    circuitBreaker.onError(durationNanos, TimeUnit.NANOSECONDS, unwrap(error));
                }
                Metadata responseTrailers = trailers.get();
                listener.onClose(
                        error == null ? Status.OK : toStatus(error),
                        responseTrailers != null ? responseTrailers : new Metadata());
            });
        }

        @Override
        public void request(int numMessages) {
            delegate.request(numMessages);
        }

        @Override
        public void cancel(String message, Throwable cause) {
            delegate.cancel(message, cause);
        }

        @Override
        public void halfClose() {
            delegate.halfClose();
        }

        @Override
        public void sendMessage(ReqT message) {
            delegate.sendMessage(message);
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }
    }
}
