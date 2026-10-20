package com.sanedge.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    JavaMailSender mailSender;
    @Mock
    EmailDedupGuard dedupGuard;
    @Mock
    KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    EmailMetrics metrics;

    private final EmailProperties properties = new EmailProperties();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, dedupGuard, kafkaTemplate, metrics,
                properties, objectMapper, "no-reply@example.test");
        properties.setRetryMaxAttempts(3);
        properties.setRetryBackoffMs(30_000);
    }

    private JsonMap payload() {
        // Event contract: standard envelope + email fields.
        return new JsonMap()
                .put("event_id", "evt-123")
                .put("schema_version", 1)
                .put("event_type", "email-service-topic-auth-register")
                .put("email", "customer@example.com")
                .put("subject", "Order Confirmation")
                .put("body", "<b>Thanks for your order!</b>");
    }

    @Nested
    @DisplayName("processRecord happy path")
    class HappyPathTests {

        @Test
        void success_claimsDedupAndMarksSent() {
            when(dedupGuard.claim("evt-123")).thenReturn(EmailDedupGuard.ClaimResult.CLAIMED);

            emailService.processRecord("src-topic", 0, 1L, "k1", payload(), 0);

            verify(mailSender).send(any(SimpleMailMessage.class));
            verify(dedupGuard).claim("evt-123");
            verify(dedupGuard).markSent("evt-123");
        }

        @Test
        void duplicate_skipsSendAndMarkSent() {
            when(dedupGuard.claim(anyString())).thenReturn(EmailDedupGuard.ClaimResult.DUPLICATE);

            emailService.processRecord("src-topic", 0, 1L, "k1", payload(), 0);

            verify(mailSender, never()).send(any(SimpleMailMessage.class));
            verify(dedupGuard, never()).markSent(anyString());
        }

        @Test
        void busyClaim_doesNotSend() {
            when(dedupGuard.claim(anyString())).thenReturn(EmailDedupGuard.ClaimResult.BUSY);

            assertThatThrownBy(() -> emailService.processRecord("src-topic", 0, 1L, "k1", payload(), 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("claim busy");

            verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }

        @Test
        void invalidEnvelope_routesToDlqWithoutSending() {
            // Missing event_id / schema_version -> invalid envelope: never sent to
            // SMTP, parked in the DLQ (event contract).
            JsonMap bad = new JsonMap().put("email", "x@y.z");

            emailService.processRecord("src-topic", 0, 1L, "k1", bad, 0);

            verify(mailSender, never()).send(any(SimpleMailMessage.class));
            verify(dedupGuard, never()).claim(anyString());

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(eq(EmailService.DLQ_TOPIC), eq("k1"), captor.capture());

            JsonMap dlq = JsonMap.fromJson(captor.getValue());
            assertThat(dlq.getString("_reason")).isEqualTo("invalid_event_envelope");
        }
    }

    @Nested
    @DisplayName("retry / DLQ routing")
    class RetryTests {

        @Test
        void sendFailure_routesToRetryTopicWithAttempt() {
            when(dedupGuard.claim("evt-123")).thenReturn(EmailDedupGuard.ClaimResult.CLAIMED);
            org.mockito.Mockito.doThrow(new RuntimeException("smtp down"))
                    .when(mailSender).send(any(SimpleMailMessage.class));

            emailService.processRecord("src-topic", 0, 1L, "k1", payload(), 0);

            verify(dedupGuard, never()).markSent(anyString());
            verify(dedupGuard).release("evt-123");

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(eq(EmailService.RETRY_TOPIC), eq("k1"), captor.capture());

            JsonMap retry = JsonMap.fromJson(captor.getValue());
            assertThat(retry.getInt("_attempt", -1)).isEqualTo(1);
            assertThat(retry.getString("_srcTopic")).isEqualTo("src-topic");
            assertThat(retry.getLong("_srcOffset", -1L)).isEqualTo(1L);
        }

        @Test
        void sendFailureAtMaxAttempts_routesToDlq() {
            when(dedupGuard.claim("evt-123")).thenReturn(EmailDedupGuard.ClaimResult.CLAIMED);
            org.mockito.Mockito.doThrow(new RuntimeException("smtp down"))
                    .when(mailSender).send(any(SimpleMailMessage.class));

            // attempt = 2, maxAttempts = 3 -> next attempt 3 hits the limit -> DLQ
            emailService.processRecord("src-topic", 0, 1L, "k1", payload(), 2);

            verify(dedupGuard).release("evt-123");

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(eq(EmailService.DLQ_TOPIC), eq("k1"), captor.capture());

            JsonMap dlq = JsonMap.fromJson(captor.getValue());
            assertThat(dlq.getString("_reason")).isEqualTo("max_retries_exceeded");
            assertThat(dlq.getInt("_attempts", -1)).isEqualTo(3);
        }
    }
}
