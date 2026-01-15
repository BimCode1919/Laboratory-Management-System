    package org.overcode250204.exception;


    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.MethodArgumentNotValidException;
    import org.springframework.web.bind.MissingServletRequestParameterException;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.RestControllerAdvice;
    import jakarta.servlet.http.HttpServletRequest;

    @RestControllerAdvice
    @Slf4j
    public class GlobalExceptionHandler {

        @Value("${spring.application.name:unknown-service}")
        private String serviceName;

        @ExceptionHandler(BaseException.class)
        public ResponseEntity<ApiErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
            ServiceErrorCode errorCode = ex.getErrorCode();

            ApiErrorResponse response = ApiErrorResponse.of(
                    serviceName,
                    errorCode,
                    request.getRequestURI()
            );

            log.warn("Business error [{}]: {} - {}", serviceName, errorCode.name(), errorCode.getMessage());
            return ResponseEntity.status(errorCode.getCode().value()).body(response);
        }


        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
            log.error("Unexpected error in [{}]: {}", serviceName, ex.getMessage(), ex);

            ApiErrorResponse response = ApiErrorResponse.of(
                    serviceName,
                    CommonErrorCode.INTERNAL_ERROR,
                    request.getRequestURI()
            );

            return ResponseEntity
                    .status(CommonErrorCode.INTERNAL_ERROR.getCode().value())
                    .body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
            log.warn("Validation error [{}]: {} - {}", serviceName, CommonErrorCode.VALIDATION_FAILED.name(), ex.getMessage());
            ApiErrorResponse response = ApiErrorResponse.of(
                    serviceName,
                    CommonErrorCode.VALIDATION_FAILED,
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiErrorResponse> handleMissingParamException(MissingServletRequestParameterException ex, HttpServletRequest request) {
            log.warn("Validation error [{}]: Missing parameter '{}'", serviceName, ex.getParameterName());

            ApiErrorResponse response = ApiErrorResponse.of(
                    serviceName,
                    CommonErrorCode.BAD_REQUEST,
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }



    }