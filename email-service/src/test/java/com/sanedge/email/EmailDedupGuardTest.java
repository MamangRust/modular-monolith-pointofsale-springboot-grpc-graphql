package com.sanedge.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class EmailDedupGuardTest {

    @Mock
    StringRedisTemplate redis;
    @Mock
    ValueOperations<String, String> valueOps;
    @Mock
    EmailMetrics metrics;

    private final EmailProperties properties = new EmailProperties();

    private EmailDedupGuard guard;

    private static final String EVENT_ID = "evt-123";
    private static final String KEY = "email:idempotency:evt-123";

    @BeforeEach
    void setUp() {
        guard = new EmailDedupGuard(redis, metrics, properties);
        properties.getIdempotency().setLeaseSeconds(60);
        properties.getIdempotency().setTtlSeconds(86_400);
    }

    private void stubValueOps() {
        when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Nested
    @DisplayName("claim (SETNX lease)")
    class ClaimTests {

        @Test
        void setnxSucceeds_returnsClaimed() {
            stubValueOps();
            when(valueOps.setIfAbsent(eq(KEY), eq("PROCESSING"), any(Duration.class))).thenReturn(true);

            assertThat(guard.claim(EVENT_ID)).isEqualTo(EmailDedupGuard.ClaimResult.CLAIMED);
        }

        @Test
        void setnxFailsOnSentState_returnsDuplicate() {
            stubValueOps();
            when(valueOps.setIfAbsent(eq(KEY), eq("PROCESSING"), any(Duration.class))).thenReturn(false);
            when(valueOps.get(KEY)).thenReturn("SENT");

            assertThat(guard.claim(EVENT_ID)).isEqualTo(EmailDedupGuard.ClaimResult.DUPLICATE);
        }

        @Test
        void setnxFailsOnActiveLease_returnsBusy() {
            stubValueOps();
            when(valueOps.setIfAbsent(eq(KEY), eq("PROCESSING"), any(Duration.class))).thenReturn(false);
            when(valueOps.get(KEY)).thenReturn("PROCESSING");
            when(redis.getExpire(KEY, TimeUnit.SECONDS)).thenReturn(30L);

            assertThat(guard.claim(EVENT_ID)).isEqualTo(EmailDedupGuard.ClaimResult.BUSY);
        }

        @Test
        void orphanedProcessingKey_isReclaimed() {
            // Crash between SETNX and EXPIRE leaves the key without TTL (ttl == -1):
            // the next claim must delete and re-claim instead of blocking forever.
            stubValueOps();
            when(valueOps.setIfAbsent(eq(KEY), eq("PROCESSING"), any(Duration.class)))
                    .thenReturn(false, true);
            when(valueOps.get(KEY)).thenReturn("PROCESSING");
            when(redis.getExpire(KEY, TimeUnit.SECONDS)).thenReturn(-1L);

            assertThat(guard.claim(EVENT_ID)).isEqualTo(EmailDedupGuard.ClaimResult.CLAIMED);
            verify(redis).delete(KEY);
        }

        @Test
        void failOpenOnRedisError_returnsClaimed() {
            stubValueOps();
            when(valueOps.setIfAbsent(eq(KEY), eq("PROCESSING"), any(Duration.class)))
                    .thenThrow(new RuntimeException("redis down"));

            assertThat(guard.claim(EVENT_ID)).isEqualTo(EmailDedupGuard.ClaimResult.CLAIMED);
            verify(metrics).recordClaimFailed();
        }
    }

    @Nested
    @DisplayName("markSent / release")
    class TerminalTests {

        @Test
        void markSent_setsSentWithTtl() {
            stubValueOps();

            guard.markSent(EVENT_ID);

            verify(valueOps).set(eq(KEY), eq("SENT"), any(Duration.class));
        }

        @Test
        void release_deletesKey() {
            guard.release(EVENT_ID);

            verify(redis).delete(KEY);
        }
    }
}
