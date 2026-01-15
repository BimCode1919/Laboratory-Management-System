package org.overcode250204.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {
    private String service;
    private String code;
    private String message;
    private int status;
    private String path;
    private Instant timestamp;

    public static ApiErrorResponse of(String serviceName, ServiceErrorCode  errorCode, String path) {
        return new ApiErrorResponse(
                serviceName,
                errorCode.name(),
                errorCode.getMessage(),
                errorCode.getCode().value(),
                path,
                Instant.now()
        );
    }


}
