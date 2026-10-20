package com.sanedge.pointofsale.auth.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Redis-backed storage for email verification codes.
 *
 * <p>Port of the blueprint auth flow's {@code RedisService} usage. Two keys per
 * registration, both 15-minute TTL:
 * {@code verification:<email>} → code and {@code verification_code:<code>} → email.
 */
@Component
@RequiredArgsConstructor
public class VerificationCodeStore {

    private static final String EMAIL_KEY_PREFIX = "verification:";
    private static final String CODE_KEY_PREFIX = "verification_code:";

    /** Matches the blueprint's 900 seconds. */
    static final Duration TTL = Duration.ofMinutes(15);

    private final StringRedisTemplate redis;

    public void save(String email, String code) {
        redis.opsForValue().set(EMAIL_KEY_PREFIX + email, code, TTL);
        redis.opsForValue().set(CODE_KEY_PREFIX + code, email, TTL);
    }

    public String resolveEmail(String code) {
        return redis.opsForValue().get(CODE_KEY_PREFIX + code);
    }

    public void consume(String email, String code) {
        redis.delete(CODE_KEY_PREFIX + code);
        redis.delete(EMAIL_KEY_PREFIX + email);
    }
}
