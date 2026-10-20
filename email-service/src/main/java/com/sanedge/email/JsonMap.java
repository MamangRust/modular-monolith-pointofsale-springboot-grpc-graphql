package com.sanedge.email;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Minimal mutable JSON map used by the email worker.
 *
 * <p>Drop-in replacement for the Vert.x {@code JsonObject} used by the original
 * Quarkus implementation, covering only the operations the worker needs.
 */
public final class JsonMap {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, Object> values;

    public JsonMap() {
        this.values = new LinkedHashMap<>();
    }

    public JsonMap(Map<String, Object> values) {
        this.values = values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
    }

    public static JsonMap fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new JsonMap();
        }
        try {
            Map<String, Object> parsed = MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            return new JsonMap(parsed);
        } catch (Exception error) {
            throw new IllegalArgumentException("Invalid JSON payload", error);
        }
    }

    public String getString(String key) {
        Object value = values.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public int getInt(String key, int defaultValue) {
        Object value = values.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        Object value = values.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public JsonMap put(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public JsonMap remove(String key) {
        values.remove(key);
        return this;
    }

    public JsonMap copy() {
        return new JsonMap(values);
    }

    public String encode() {
        try {
            return MAPPER.writeValueAsString(values);
        } catch (Exception error) {
            throw new IllegalStateException("Failed to encode JSON payload", error);
        }
    }

    public String getEventId() {
        return getString("event_id");
    }

    /** Raw backing map (read-only use). */
    public Map<String, Object> asMap() {
        return values;
    }
}
