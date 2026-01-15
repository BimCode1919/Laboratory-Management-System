package org.overcode250204.warehouseservice.exceptions;

import lombok.Getter;
import org.overcode250204.exception.ServiceErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode implements ServiceErrorCode {
    GATEWAY_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "Gateway signature invalid"),
    USER_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "User is not authenticated"),
    GET_USER_ID_FROM_IAM_SERVICE_ERROR(HttpStatus.BAD_REQUEST, "Get user id from iam service error"),
    INSTRUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Instrument not found"),
    CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND, "Configuration not found"),
    NO_GLOBAL_CONFIG_FOUND(HttpStatus.BAD_REQUEST, "No global configurations available to clone"),
    INSTRUMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "Instrument name or serial number already exists"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Request cannot be null"),
    REAGENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Reagent not found"),
    REAGENT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "Not enough reagent"),
    NO_DATA(HttpStatus.NO_CONTENT, "No data available"), REAGENT_NAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Reagent name already exists"),
    INVALID_REAGENT_REQUEST(HttpStatus.BAD_REQUEST, "Invalid reagent request"),
    VENDOR_NOT_FOUND(HttpStatus.NOT_FOUND, "Vendor not found"),
    VENDOR_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Vendor name already exists"),
    ;

    private final HttpStatusCode code;
    private final String message;

    ErrorCode(HttpStatusCode code, String message) {
        this.code = code;
        this.message = message;
    }
}
