package org.overcode250204.testorderservice.exceptions;

import lombok.Getter;
import org.overcode250204.exception.ServiceErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode implements ServiceErrorCode {
    GATEWAY_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "Gateway signature invalid"),
    USER_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "User is not authenticated"),
    GET_USER_ID_FROM_IAM_SERVICE_ERROR(HttpStatus.BAD_REQUEST, "Get user id from iam service error"),
    FAIL_TO_CREAT_TEST_ORDER(HttpStatus.BAD_REQUEST, "Fail to create test order"),
    FAIL_TO_PUBLISH_TEST_ORDER_COMPLETION(HttpStatus.BAD_REQUEST, "Fail to publish test order completion"),
    HL7_PARSING_FAILED(HttpStatus.BAD_REQUEST, "HL7 parsing failed"),
    FAIL_TO_LISTEN_HL7_TEST_RESULT_PUBLISH(HttpStatus.BAD_REQUEST, "Fail to listen hl7 test result publish"),
    ERROR_TO_PROCESS_RAW_RESULT_MESSAGE_FROM_INSTRUMENT(HttpStatus.BAD_REQUEST, "Error to process raw result message from instrument"),
    EXPORT_CSV_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export CSV to S3"),
    EXPORT_EXCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export XLSX to S3"),
    EXPORT_PDF_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export PDF to S3"),
    EXPORT_PDF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PDF export is only allowed for completed test orders"),
    RAW_RESULTS_NOT_READY_FOR_PROCESSING(HttpStatus.BAD_REQUEST, "Raw results not ready for processing"),
    FAIL_TO_CREAT_AUDIT_LOG(HttpStatus.INTERNAL_SERVER_ERROR, "Fail to create audit log"),
    PATIENT_CODE_DOES_NOT_EXIST(HttpStatus.BAD_REQUEST, "Patient code doesn't exist"),
    FAIL_TO_SYNC_UPDATED_PATIENT_INFORMATION(HttpStatus.INTERNAL_SERVER_ERROR, "Fail to sync patient information"),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "Start time cannot be after end time"),
    TEST_NOT_FOUND(HttpStatus.NOT_FOUND, "Test order not found"),
    EVENT_ID_MISSING(HttpStatus.BAD_REQUEST, "Event ID is missing"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment not found"),
    PAYMENT_CAPTURE_FAILED(HttpStatus.BAD_REQUEST, "Payment capture failed"),
    PRICING_NOT_FOUND(HttpStatus.NOT_FOUND, "Test pricing not found"),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "Invalid price for the test"),
    PAYMENT_INVALID_ORDER(HttpStatus.BAD_REQUEST, "Payment does not belong to the specified test order"),
    PAYMENT_CREATE_FAILED(HttpStatus.BAD_REQUEST, "Payment creation failed"),
    PAYMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "Payment already exists for this test order"),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "Payment amount does not match the test order amount"),
    WEBHOOK_INVALID_EVENT(HttpStatus.BAD_REQUEST, "Webhook contains an invalid event"),
    WEBHOOK_MISSING_ORDER_ID(HttpStatus.BAD_REQUEST, "Webhook is missing order ID"),
    UNAUTHORIZED_PAYMENT(HttpStatus.UNAUTHORIZED, "Unauthorized payment access"),
    RAW_TEST_RESULTS_NOT_FOUND(HttpStatus.NOT_FOUND, "Raw test results not found"),
    ;


    private final HttpStatusCode code;
    private final String message;

    ErrorCode(HttpStatusCode code, String message) {
        this.code = code;
        this.message = message;
    }
}
