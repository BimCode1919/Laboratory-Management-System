package org.overcode250204.gatewayservice.exceptions;

import org.overcode250204.base.BaseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.application.name}")
    private String serviceName;

    @ExceptionHandler(Exception.class)
    public Mono<BaseResponse<?>> handleException(Exception e) {
        return Mono.just(BaseResponse.error(
                serviceName,
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                e.getMessage()
        ));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<BaseResponse<?>> handleBadRequestException(BadRequestException e) {
        return Mono.just(BaseResponse.error(
                serviceName,
                HttpStatus.BAD_REQUEST.toString(),
                e.getMessage()
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<BaseResponse<?>> handleResourceNotFoundException(ResourceNotFoundException e) {
        return Mono.just(BaseResponse.error(
                serviceName,
                HttpStatus.NOT_FOUND.toString(),
                e.getMessage()
        ));
    }

    @ExceptionHandler(JwtVerificationException.class)
    public Mono<BaseResponse<?>> handleJwtVerificationException(JwtVerificationException e) {
        return Mono.just(BaseResponse.error(
                serviceName,
                HttpStatus.UNAUTHORIZED.toString(),
                e.getMessage()
        ));
    }

    @ExceptionHandler(GenerationSignatureException.class)
    public Mono<BaseResponse<?>> handleGenerationSignatureException(GenerationSignatureException e) {
        return Mono.just(BaseResponse.error(
                serviceName,
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                e.getMessage()
        ));
    }

}
