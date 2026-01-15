package org.overcode250204.base;

import lombok.Getter;
import lombok.Builder;
import org.springframework.http.HttpStatus;


import java.time.Instant;

@Getter
@Builder
public class BaseResponse<T> {
    private String service;
    private String code;
    private String message;
    private T data;
    private Instant timestamp;

    public static <T> BaseResponse<T> success(String serviceName, T data) {
        return BaseResponse.<T>builder()
                .service(serviceName)
                .code("200")
                .message("Operation completed successfully")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> BaseResponse<T> of(String serviceName, String code, String message, T data) {
        return BaseResponse.<T>builder()
                .service(serviceName)
                .code(code)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static BaseResponse<?> error(String serviceName, String code, String message) {
        return BaseResponse.builder()
                .service(serviceName)
                .code(code)
                .message(message)
                .data(null)
                .timestamp(Instant.now())
                .build();
    }
}


