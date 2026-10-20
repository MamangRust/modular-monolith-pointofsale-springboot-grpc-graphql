package com.sanedge.pointofsale.common.exception;

public class RefreshTokenException extends ResourceNotFoundException {
    private final String token;

    public RefreshTokenException(String token, String message) {
        super(message);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}