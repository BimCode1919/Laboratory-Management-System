package org.overcode250204.instrumentservice.exception;

import lombok.Getter;
import org.overcode250204.exception.ServiceErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode implements ServiceErrorCode {

    INSTRUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Instrument not found"),
    INSTRUMENT_BUSY(HttpStatus.CONFLICT, "Instrument is currently running"),
    CONFIG_SYNC_FAILED(HttpStatus.BAD_GATEWAY, "Failed to sync configuration"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request data"),
    INVALID_MODE_TRANSITION(HttpStatus.BAD_REQUEST, "Invalid mode transition"),
    KAFKA_PUBLISH_FAILED(HttpStatus.BAD_GATEWAY, "Failed to publish event to Kafka"),
    INSTRUMENT_NOT_READY(HttpStatus.CONFLICT, "Instrument not ready"),
    RUN_NOT_FOUND(HttpStatus.NOT_FOUND, "Instrument run not found"),
    INVALID_RUN_STATE(HttpStatus.BAD_REQUEST, "Invalid instrument run state"),
    RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "Test result not found"),
    RESULT_NOT_BACKED_UP(HttpStatus.CONFLICT, "Test result not backed up"),
    REAGENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Reagent not found"),
    REAGENT_EXPIRED(HttpStatus.CONFLICT, "Reagent is expired"),
    REAGENT_NOT_INSTALLED(HttpStatus.BAD_REQUEST, "Reagent not installed"),
    REAGENT_STATUS_NO_CHANGE(HttpStatus.BAD_REQUEST, "Reagent status no change"),
    REAGENT_LOW(HttpStatus.CONFLICT, "Reagent level is low"),
    RAW_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "Raw test result not found"),
    WAREHOUSE_VALIDATION_FAILED(HttpStatus.CONFLICT, "Warehouse validation failed"),
    GATEWAY_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "Gateway signature invalid"),
    USER_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "User is not authenticated"),
    GET_USER_ID_FROM_IAM_SERVICE_ERROR(HttpStatus.BAD_REQUEST, "Get user id from iam service error"),
    BARCODE_NOT_FOUND(HttpStatus.NOT_FOUND, "Barcode not found"),
    BARCODE_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "Barcode list not found"),
    REAGENT_INSTALL_PUBLISH_FAILED(HttpStatus.BAD_GATEWAY, "Failed to publish reagent install request"),
    REAGENT_SYNC_PUBLISH_FAILED(HttpStatus.BAD_GATEWAY, "Failed to publish reagent sync request"),
    REAGENT_UNINSTALL_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "Uninstall quantity exceeds current quantity"),
    INSTRUMENT_CONFIGURATION_SYNC_FAILED(HttpStatus.BAD_GATEWAY, "Failed to publish instrument configuration sync request"),
    INSTRUMENT_CONFIGURATION_ALL_SYNC_FAILED(HttpStatus.BAD_GATEWAY, "Failed to publish instrument all-configuration sync request"),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "Quantity must be a positive number")

    ;

    private final HttpStatusCode code;
    private final String message;

    ErrorCode(HttpStatusCode code, String message) {
        this.code = code;
        this.message = message;
    }
}
