package com.heroengineer.hero_engineer_backend.jwt;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        cause.printStackTrace();
    }
}

