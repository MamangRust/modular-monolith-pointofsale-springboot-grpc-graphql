package com.sanedge.pointofsale.common.security;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtProvider {
    static final String issuer = "MyApp";

    @Value("${springjwt.app.jwtSecret}")
    private String jwtSecret;

    @Value("${springjwt.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${springjwt.app.jwtRefreshSecret}")
    private String jwtRefreshSecret;

    @Value("${springjwt.app.jwtRefreshExpirationMs}")
    private int jwtRefreshExpirationMs;

    private Algorithm accessTokenAlgorithm;
    private Algorithm refreshTokenAlgorithm;

    private JWTVerifier jwtTokenVerifier;
    private JWTVerifier jwtRefreshTokenVerifier;

    public JwtProvider(
            @Value("${springjwt.app.jwtSecret:0a5371c29751174b5601acb50153c0e66e4f6d6525ed08485e63d94a70985c31}") String accessTokenSecret,
            @Value("${springjwt.app.jwtExpirationMs:86400000}") int jwtExpirationMs,
            @Value("${springjwt.app.jwtRefreshSecret:0a5371c29751174b5601acb50153c0e66e4f6d6525ed08485e63d94a70985c32}") String refreshTokenSecret,
            @Value("${springjwt.app.jwtRefreshExpirationMs:86400000}") int jwtRefreshExpirationMs) {

        this.jwtExpirationMs = jwtExpirationMs * 60 * 1000;
        this.jwtRefreshExpirationMs = jwtRefreshExpirationMs * 60 * 1000;

        this.accessTokenAlgorithm = Algorithm.HMAC512(accessTokenSecret);
        this.refreshTokenAlgorithm = Algorithm.HMAC512(refreshTokenSecret);

        this.jwtTokenVerifier = JWT.require(accessTokenAlgorithm)
                .withIssuer(issuer)
                .build();

        this.jwtRefreshTokenVerifier = JWT.require(refreshTokenAlgorithm)
                .withIssuer(issuer)
                .build();
    }

    public String generateAccessToken(String username) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(accessTokenAlgorithm);
    }

    public boolean validateAccessToken(String token) {
        return decodeToken(token, jwtTokenVerifier).isPresent();
    }

    public String getUsernameAccessToken(String token) {
        return decodeToken(token, jwtTokenVerifier).get().getSubject();
    }

    public int getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public String generateRefreshToken(String username) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
                .sign(refreshTokenAlgorithm);
    }

    public boolean validateRefreshToken(String token) {
        return decodeToken(token, jwtRefreshTokenVerifier).isPresent();
    }

    public String getUsernameRefreshToken(String token) {
        return decodeToken(token, jwtRefreshTokenVerifier).get().getSubject();
    }

    public int getJwtRefreshExpirationMs() {
        return jwtRefreshExpirationMs;
    }

    private Optional<DecodedJWT> decodeToken(String token, JWTVerifier verifier) {
        try {
            return Optional.of(verifier.verify(token));
        } catch (JWTVerificationException e) {
            log.error("❌ Invalid token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
