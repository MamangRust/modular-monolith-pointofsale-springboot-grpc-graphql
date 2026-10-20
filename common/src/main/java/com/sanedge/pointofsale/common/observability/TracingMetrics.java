package com.sanedge.pointofsale.common.observability;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;

/**
 * Spring Boot port of the Quarkus {@code com.sanedge.common.observability.TracingMetrics}.
 *
 * <p>Records a span (with the same {@code status}/{@code duration_secs} event as the
 * Quarkus version) plus the {@code requests_total} counter and
 * {@code request_duration_seconds} histogram for each wrapped operation.
 *
 * <p>Spring Boot auto-configures an {@link OpenTelemetry} bean when
 * {@code spring-boot-starter-actuator} and {@code micrometer-tracing-bridge-otel} are on
 * the classpath. To keep modules bootable even without an exporter, the bean is looked up
 * through an {@link ObjectProvider} and falls back to {@link OpenTelemetry#noop()}.
 */
@Component
public class TracingMetrics {

    private static final AttributeKey<String> METHOD_KEY = AttributeKey.stringKey("method");
    private static final AttributeKey<String> STATUS_KEY = AttributeKey.stringKey("status");

    private final Tracer tracer;
    private final Meter meter;
    private final LongCounter requestCounter;
    private final DoubleHistogram requestDurationHistogram;
    private final TextMapPropagator propagator;

    public TracingMetrics(ObjectProvider<OpenTelemetry> openTelemetryProvider,
            @Value("${spring.application.name:pointofsale-service}") String applicationName) {
        OpenTelemetry openTelemetry = openTelemetryProvider.getIfAvailable(OpenTelemetry::noop);

        this.tracer = openTelemetry.getTracer(applicationName);
        this.meter = openTelemetry.getMeter(applicationName);
        this.propagator = openTelemetry.getPropagators().getTextMapPropagator();

        this.requestCounter = meter.counterBuilder("requests_total")
                .setDescription("Total number of requests")
                .build();

        this.requestDurationHistogram = meter.histogramBuilder("request_duration_seconds")
                .setDescription("Request duration in seconds")
                .setUnit("s")
                .build();
    }

    public Tracer getTracer() {
        return tracer;
    }

    private static final TextMapGetter<Map<String, String>> MAP_GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier.keySet();
        }

        @Override
        public String get(Map<String, String> carrier, String key) {
            return carrier != null ? carrier.get(key) : null;
        }
    };

    public void injectContext(Context context, Map<String, String> carrier) {
        propagator.inject(Objects.requireNonNull(context), Objects.requireNonNull(carrier), Map::put);
    }

    public Context extractContext(Map<String, String> carrier) {
        return Objects.requireNonNull(propagator.extract(
                Objects.requireNonNull(Context.current()),
                Objects.requireNonNull(carrier),
                Objects.requireNonNull(MAP_GETTER)));
    }

    public TracingContext startSpan(String operationName) {
        return startSpan(operationName, Attributes.empty());
    }

    public TracingContext startSpan(String operationName, Attributes attributes) {
        Instant startTime = Instant.now();

        Span span = tracer.spanBuilder(Objects.requireNonNull(operationName))
                .setSpanKind(SpanKind.INTERNAL)
                .setAllAttributes(Objects.requireNonNull(attributes))
                .startSpan();

        Context context = Context.current().with(span);

        return new TracingContext(context, startTime);
    }

    public <T> T traceAndMeasure(String operationName, String method, Supplier<T> supplier) {
        return traceAndMeasure(operationName, method, Attributes.empty(), supplier);
    }

    public <T> T traceAndMeasure(String operationName, String method, Attributes attributes,
            Supplier<T> supplier) {
        TracingContext tracingContext = startSpan(operationName, attributes);

        try (Scope scope = tracingContext.getContext().makeCurrent()) {
            T result = supplier.get();
            completeSpanSuccess(tracingContext, method, "Operation completed successfully");
            return result;
        } catch (Exception e) {
            completeSpanError(tracingContext, method, "Operation failed: " + e.getMessage());
            throw e;
        }
    }

    /** Alias kept for parity with the Quarkus API, which exposes a synchronous variant. */
    public <T> T traceAndMeasureSync(String operationName, String method, Supplier<T> supplier) {
        return traceAndMeasure(operationName, method, supplier);
    }

    public <T> T traceAndMeasureSync(String operationName, String method, Attributes attributes,
            Supplier<T> supplier) {
        return traceAndMeasure(operationName, method, attributes, supplier);
    }

    public void completeSpanSuccess(TracingContext tracingContext, String method, String message) {
        completeSpan(tracingContext, method, true, message);
    }

    public void completeSpanError(TracingContext tracingContext, String method, String errorMessage) {
        completeSpan(tracingContext, method, false, errorMessage);
    }

    private void completeSpan(TracingContext tracingContext, String method, boolean isSuccess, String message) {
        String status = isSuccess ? "SUCCESS" : "ERROR";
        double duration = Duration.between(tracingContext.getStartTime(), Instant.now()).toMillis() / 1000.0;

        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        span.addEvent("Operation completed", Attributes.builder()
                .put("status", status)
                .put("duration_secs", duration)
                .put("message", message)
                .build());

        if (isSuccess) {
            span.setStatus(StatusCode.OK);
        } else {
            span.setStatus(StatusCode.ERROR, message);
        }

        Attributes metricAttributes = Attributes.builder()
                .put(METHOD_KEY, method)
                .put(STATUS_KEY, status)
                .build();

        requestCounter.add(1, metricAttributes);
        requestDurationHistogram.record(duration, metricAttributes);

        span.end();
    }

    public static class TracingContext {
        private final Context context;
        private final Instant startTime;

        public TracingContext(Context context, Instant startTime) {
            this.context = context;
            this.startTime = startTime;
        }

        public Context getContext() {
            return context;
        }

        public Instant getStartTime() {
            return startTime;
        }
    }
}
