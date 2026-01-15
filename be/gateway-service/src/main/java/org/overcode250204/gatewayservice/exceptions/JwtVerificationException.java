package org.overcode250204.gatewayservice.exceptions;

public class JwtVerificationException extends RuntimeException {
    public JwtVerificationException(String message) {
        super(message);
    }
    public JwtVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
