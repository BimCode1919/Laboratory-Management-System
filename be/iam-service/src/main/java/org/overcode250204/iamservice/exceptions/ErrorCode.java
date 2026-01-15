package org.overcode250204.iamservice.exceptions;

import com.google.api.Http;
import lombok.Getter;
import lombok.Setter;
import org.overcode250204.exception.ServiceErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode implements ServiceErrorCode {
    AES_ENCRYPT_FAILED(HttpStatus.NOT_FOUND, "User not found with identity number"),
    AES_DECRYPT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AES Decryption failed"),
    GATEWAY_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "Gateway signature invalid"),
    USER_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "User is not authenticated"),
    EMAIL_EXISTS(HttpStatus.BAD_REQUEST, "Email already exists."),
    COGNITO_SUB_DOES_NOT_EXIST(HttpStatus.BAD_REQUEST, "Cognito Sub Does Not Exist"),
    UUID_PATH_STRING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "UUID path to string failed"),
    USER_NOT_FOUND_WITH_ID(HttpStatus.NOT_FOUND, "User not found with id"),
    USER_ALREADY_DISABLE(HttpStatus.ALREADY_REPORTED, "User already disable"),
    USER_ALREADY_ACTIVE(HttpStatus.ALREADY_REPORTED, "User already active"),
    FAILED_TO_DISABLE(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to disable user"),
    FAILED_TO_ENABLE(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to enable user"),
    ROLE_CODE_EXISTED(HttpStatus.ALREADY_REPORTED, "Role code already exist"),
    PRIVILEGE_DEFAULT_NOT_FOUND(HttpStatus.NOT_FOUND, "Default privilege READ_ONLY not found"),
    INVALID_PRIVILEGE_ID(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid privilege IDs"),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Role not found"),
    FAIL_TO_CREATE_AUDIT_LOGS_SEND_TO_MONITORING_SERVICE(HttpStatus.BAD_REQUEST, "Fail to create audit logs send to monitoring service!!!"),
    FAIL_TO_LOGIN(HttpStatus.UNAUTHORIZED, "Login failed"),
    FAIL_TO_FORGOT_PASSWORD_FROM_COGNITO(HttpStatus.BAD_REQUEST, "Forgot Password From Cognito"),
    FORGOT_PASSWORD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Forgot Password failed"),
    FAIL_TO_CONFIRM_PASSWORD_FROM_COGNITO(HttpStatus.BAD_REQUEST, "Forgot Password From Cognito"),
    CONFIRM_PASSWORD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Confirm Password failed"),
    FAIL_TO_REFRESH_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid or expired refresh token"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Refresh token not found."),
    INVALID_REFRESH_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid refresh token."),
    FAIL_TO_SAVE_OUTBOX(HttpStatus.INTERNAL_SERVER_ERROR, "Fail to save outbox!"),
    EMAIL_ALREADY_USED(HttpStatus.ALREADY_REPORTED, "Email already used"),
    ;
    private final HttpStatusCode code;
    private final String message;

    ErrorCode(HttpStatusCode code, String message) {
        this.code = code;
        this.message = message;
    }
}
