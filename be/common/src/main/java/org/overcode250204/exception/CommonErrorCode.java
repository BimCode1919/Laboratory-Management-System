package org.overcode250204.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode implements ServiceErrorCode{
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request data"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication failed. A valid token is required."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access Denied. You do not have permission."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed. Check your input.");
    ;
    private final HttpStatus code;
    private final String message;

    CommonErrorCode(HttpStatus code, String message) {
        this.code = code;
        this.message = message;
    }
}
