package com.sanedge.pointofsale.auth.service.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanedge.pointofsale.auth.domain.requests.AuthRequest;
import com.sanedge.pointofsale.auth.domain.requests.RegisterRequest;
import com.sanedge.pointofsale.auth.domain.requests.ResetPasswordRequest;
import com.sanedge.pointofsale.auth.domain.responses.TokenResponse;
import com.sanedge.pointofsale.auth.domain.responses.UserResponse;
import com.sanedge.pointofsale.auth.models.RefreshToken;
import com.sanedge.pointofsale.auth.models.ResetToken;
import com.sanedge.pointofsale.auth.repository.RefreshTokenCommandRepository;
import com.sanedge.pointofsale.auth.repository.RefreshTokenQueryRepository;
import com.sanedge.pointofsale.auth.repository.ResetTokenRepository;
import com.sanedge.pointofsale.auth.service.AuthService;
import com.sanedge.pointofsale.auth.service.VerificationCodeStore;
import com.sanedge.pointofsale.common.adapter.model.CreateUserCmd;
import com.sanedge.pointofsale.common.adapter.model.UpdateUserCmd;
import com.sanedge.pointofsale.common.adapter.model.User;
import com.sanedge.pointofsale.common.adapter.model.VerifyPasswordResult;
import com.sanedge.pointofsale.common.adapter.support.AdapterException;
import com.sanedge.pointofsale.common.adapter.user.UserCommandPort;
import com.sanedge.pointofsale.common.adapter.user.UserQueryPort;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.common.observability.TracingMetrics;
import com.sanedge.pointofsale.common.outbox.Outbox;
import com.sanedge.pointofsale.common.outbox.OutboxEventFactory;
import com.sanedge.pointofsale.common.outbox.OutboxRepository;
import com.sanedge.pointofsale.common.security.JwtProvider;

import io.opentelemetry.api.common.Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Auth domain logic.
 *
 * <p>Email side effects (welcome, forgot-password, verify-code confirmation) are
 * written to the shared {@code outbox} table in the same transaction and relayed
 * to Kafka by {@link com.sanedge.pointofsale.common.outbox.OutboxPublisher}.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthImplService implements AuthService {

    private static final String TOPIC_AUTH_REGISTER = "email-service-topic-auth-register";
    private static final String TOPIC_AUTH_FORGOT_PASSWORD = "email-service-topic-auth-forgot-password";
    private static final String TOPIC_AUTH_VERIFY_CODE_SUCCESS = "email-service-topic-auth-verify-code-success";

    private static final long RESET_TOKEN_TTL_MS = 900_000L; // 15 minutes

    private final UserQueryPort userQueryPort;
    private final UserCommandPort userCommandPort;
    private final JwtProvider jwtProvider;
    private final RefreshTokenQueryRepository refreshTokenQueryRepository;
    private final RefreshTokenCommandRepository refreshTokenCommandRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final VerificationCodeStore verificationCodeStore;
    private final OutboxRepository outboxRepository;
    private final TracingMetrics tracingMetrics;

    @Override
    public ApiResponse<TokenResponse> login(AuthRequest loginRequest) {
        Attributes attrs = Attributes.builder()
                .put("user.username", loginRequest.getUsername() != null ? loginRequest.getUsername() : "")
                .build();

        return tracingMetrics.traceAndMeasure("loginUser", "login", attrs, () -> {
            VerifyPasswordResult verification = userQueryPort.verifyPassword(
                    loginRequest.getUsername(), loginRequest.getPassword());

            if (!verification.valid()) {
                throw new IllegalArgumentException("Invalid username or password");
            }

            String accessToken = jwtProvider.generateAccessToken(verification.username());
            String refreshTokenStr = jwtProvider.generateRefreshToken(verification.username());

            Long userId = (long) verification.userId();

            RefreshToken refreshTokenEntity = refreshTokenQueryRepository.findByUserId(userId)
                    .orElseGet(RefreshToken::new);
            refreshTokenEntity.setUserId(userId);
            refreshTokenEntity.setToken(refreshTokenStr);
            refreshTokenEntity.setExpiration(
                    new Timestamp(System.currentTimeMillis() + jwtProvider.getJwtRefreshExpirationMs()));
            refreshTokenEntity = refreshTokenCommandRepository.save(refreshTokenEntity);

            TokenResponse tokenResponse = TokenResponse.builder()
                    .access_token(accessToken)
                    .refresh_token(refreshTokenEntity.getToken())
                    .build();

            return ApiResponse.<TokenResponse>builder()
                    .status("success")
                    .message("Login successful")
                    .data(tokenResponse)
                    .build();
        });
    }

    @Override
    public ApiResponse<UserResponse> register(RegisterRequest registerRequest) {
        Attributes attrs = Attributes.builder()
                .put("user.email", registerRequest.getEmail() != null ? registerRequest.getEmail() : "")
                .build();

        return tracingMetrics.traceAndMeasure("registerUser", "register", attrs, () -> {
            User created;
            try {
                created = userCommandPort.create(new CreateUserCmd(
                        registerRequest.getUsername(),
                        registerRequest.getFirstname(),
                        registerRequest.getLastname(),
                        registerRequest.getEmail(),
                        registerRequest.getPassword(),
                        registerRequest.getConfirmPassword()));
            } catch (AdapterException e) {
                throw new IllegalArgumentException(e.getMessage());
            }

            String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            verificationCodeStore.save(created.email(), verificationCode);
            sendWelcomeEmail(created, verificationCode);

            return ApiResponse.<UserResponse>builder()
                    .status("success")
                    .message("User registered successfully")
                    .data(UserResponse.from(created))
                    .build();
        });
    }

    @Override
    public ApiResponse<TokenResponse> refreshToken(String refreshToken) {
        Attributes attrs = Attributes.builder()
                .put("operation", "refresh")
                .build();

        return tracingMetrics.traceAndMeasure("refreshToken", "refresh", attrs, () -> {
            RefreshToken storedToken = refreshTokenQueryRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

            if (storedToken.getExpiration().before(new Date())) {
                log.warn("❌ [REFRESH TOKEN] Token expired | Token: {}", refreshToken);
                throw new IllegalArgumentException("Refresh token expired");
            }

            User user = userQueryPort.findById(storedToken.getUserId().intValue());

            String newAccessToken = jwtProvider.generateAccessToken(user.username());

            Date newExpiration = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
            storedToken.setExpiration(new Timestamp(newExpiration.getTime()));
            refreshTokenCommandRepository.save(storedToken);

            TokenResponse tokenResponse = TokenResponse.builder()
                    .access_token(newAccessToken)
                    .refresh_token(refreshToken)
                    .build();

            return ApiResponse.<TokenResponse>builder()
                    .status("success")
                    .message("Access token refreshed successfully")
                    .data(tokenResponse)
                    .build();
        });
    }

    @Override
    public ApiResponse<UserResponse> getCurrentUser(String username) {
        Attributes attrs = Attributes.builder()
                .put("user.username", username != null ? username : "")
                .build();

        return tracingMetrics.traceAndMeasure("getCurrentUser", "get_current_user", attrs, () -> {
            User user = userQueryPort.findByUsername(username);

            return ApiResponse.<UserResponse>builder()
                    .status("success")
                    .message("Current user fetched successfully")
                    .data(UserResponse.from(user))
                    .build();
        });
    }

    @Override
    @Transactional
    public ApiResponse<Void> forgotPassword(String email) {
        return tracingMetrics.traceAndMeasure("forgotPassword", "forgot_password", () -> {
            User user = userQueryPort.findByEmail(email);
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }

            String token = UUID.randomUUID().toString();

            resetTokenRepository.deleteByUserId((long) user.id());
            ResetToken resetToken = new ResetToken();
            resetToken.setUserId((long) user.id());
            resetToken.setToken(token);
            resetToken.setExpiration(new Timestamp(System.currentTimeMillis() + RESET_TOKEN_TTL_MS));
            resetTokenRepository.save(resetToken);

            sendForgotPasswordEmail(user, token);

            return ApiResponse.<Void>builder()
                    .status("success")
                    .message("Password reset email sent successfully")
                    .build();
        });
    }

    @Override
    @Transactional
    public ApiResponse<Void> resetPassword(ResetPasswordRequest resetPasswordRequest) {
        return tracingMetrics.traceAndMeasure("resetPassword", "reset_password", () -> {
            if (!resetPasswordRequest.getPassword().equals(resetPasswordRequest.getConfirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match");
            }

            ResetToken resetToken = resetTokenRepository.findByToken(resetPasswordRequest.getToken())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

            if (resetToken.getExpiration().before(new Date())) {
                throw new IllegalArgumentException("Invalid or expired reset token");
            }

            User user = userQueryPort.findById(resetToken.getUserId().intValue());
            userCommandPort.update(new UpdateUserCmd(
                    user.id(),
                    user.username(),
                    user.firstname(),
                    user.lastname(),
                    user.email(),
                    resetPasswordRequest.getPassword(),
                    resetPasswordRequest.getConfirmPassword()));

            resetTokenRepository.delete(resetToken);

            return ApiResponse.<Void>builder()
                    .status("success")
                    .message("Password reset successfully")
                    .build();
        });
    }

    @Override
    public ApiResponse<Void> verifyCode(String code) {
        return tracingMetrics.traceAndMeasure("verifyEmailByCode", "verify_email", () -> {
            String email = verificationCodeStore.resolveEmail(code);
            if (email == null) {
                throw new IllegalArgumentException("Invalid or expired verification code");
            }

            verificationCodeStore.consume(email, code);
            sendVerifyCodeSuccessEmail(email);

            return ApiResponse.<Void>builder()
                    .status("success")
                    .message("Email verified successfully")
                    .build();
        });
    }

    private void sendWelcomeEmail(User user, String code) {
        String subject = "Welcome to Point of Sale";
        String body = String.format(
                "Hello %s %s,\n\nWelcome to our platform! Use the following code to verify your email address:\n\n%s\n\nRegards,\nSupport Team",
                user.firstname(), user.lastname(), code);

        enqueueEmail(TOPIC_AUTH_REGISTER, user.email(), String.valueOf(user.id()), subject, body);
    }

    private void sendForgotPasswordEmail(User user, String token) {
        String subject = "Reset Password Verification";
        String body = String.format(
                "Hello %s %s,\n\nYou have requested a password reset. Use the following token to reset your password:\n\n%s\n\nThis token will expire in 15 minutes.\n\nRegards,\nSupport Team",
                user.firstname(), user.lastname(), token);

        enqueueEmail(TOPIC_AUTH_FORGOT_PASSWORD, user.email(), String.valueOf(user.id()), subject, body);
    }

    /** Blueprint produced no verify-code topic; payload follows the shared envelope. */
    private void sendVerifyCodeSuccessEmail(String email) {
        String subject = "Email Verified Successfully";
        String body = "Hello,\n\nYour email address has been verified successfully. "
                + "You can now use all features of your account.\n\nRegards,\nSupport Team";

        enqueueEmail(TOPIC_AUTH_VERIFY_CODE_SUCCESS, email, email, subject, body);
    }

    /** Writes one email event to the shared outbox; the relay publishes it. */
    private void enqueueEmail(String topic, String email, String aggregateId, String subject, String body) {
        OutboxEventFactory.Envelope envelope = OutboxEventFactory.email(topic, email, subject, body);

        Outbox event = new Outbox();
        event.setAggregateType("USER");
        event.setAggregateId(aggregateId);
        event.setTopic(topic);
        event.setPayload(envelope.payload());
        outboxRepository.enqueue(event);
    }
}
