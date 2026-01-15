package org.overcode250204.patientservice.exceptions;

import lombok.Getter;
import org.overcode250204.exception.ServiceErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode implements ServiceErrorCode {
    GATEWAY_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "Gateway signature invalid"),
    GET_USER_ID_FROM_IAM_SERVICE_ERROR(HttpStatus.BAD_REQUEST, "Get user id from iam service error"),
    USER_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "User is not authenticated"),
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "Patient Medical Record not found"),
    DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Deletion not allowed due to linked active test orders"),
    PATIENT_MUST_MEDICAL_RECORD_EXISTS(HttpStatus.BAD_REQUEST, "Patient Must Medical Record exists"),
    GET_MESSAGE_FROM_KAFKA_TOPIC_TEST_ORDER_CREATED_ERROR(HttpStatus.BAD_REQUEST, "Get message from kafka order created"),
    PATIENT_DOES_NOT_EXIST(HttpStatus.BAD_REQUEST, "Patient does not exist"),
    PATIENT_CODE_REQUEST_NOT_MATCHING_WITH_PATIENT_CODE_OF_MEDICAL_RECORD(HttpStatus.BAD_REQUEST, "Patient code does not match with patient code of medical record"),
    BACKUP_MEDICAL_RECORD_DATA_FAIL(HttpStatus.BAD_REQUEST, "Backup medical record data fail"),
    FAIL_TO_LISTEN_TEST_ORDER_RESULTS_COMPLETED_EVENT(HttpStatus.BAD_REQUEST, "Fail to listen test order results completed"),
    MEDICAL_RECORD_NOT_FOUND(HttpStatus.BAD_REQUEST, "Patient Medical Record not found"),
    FAIL_TO_SAVE_TEST_RECORD(HttpStatus.BAD_REQUEST, "Fail to save test record"),
    PATIENT_EXISTED(HttpStatus.BAD_REQUEST, "Patient already exists because dob and email or phone is duplicated"),
    ERROR_TO_FIND_EXISTED_PATIENT(HttpStatus.BAD_REQUEST, "Error to find existed patient because of an error"),
    ERROR_TO_CREATE_MEDICAL_RECORD(HttpStatus.BAD_REQUEST, "Error to create medical record because of an error"),
    INVALID_PARSE_TO_TEST_RECORD_STATUS(HttpStatus.BAD_REQUEST, "Invalid status value"),
    FAIL_TO_CREATE_OUTBOX_EVENT_TO_SEND_MONITORING_SERVICE(HttpStatus.BAD_REQUEST, "Error to create outbox event to send monitoring service"),
    EMAIL_ALREADY_USED(HttpStatus.BAD_REQUEST, "Email already used"),
    PHONE_ALREADY_USED(HttpStatus.BAD_REQUEST, "Phone already used"),
    AES_ENCRYPT_FAILED(HttpStatus.NOT_FOUND, "User not found with identity number"),
    AES_DECRYPT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AES Decryption failed"),
    PATIENT_CODE_DOES_NOT_EXIST(HttpStatus.BAD_REQUEST, "Patient code does not exist"),
    EMAIL_IS_USED(HttpStatus.BAD_REQUEST, "Email is used"),
    PHONE_IS_USED(HttpStatus.BAD_REQUEST, "Phone is used"),
    FAIL_TO_CREATE_AUDIT_LOGS_SEND_TO_MONITORING_SERVICE(HttpStatus.BAD_REQUEST, "Fail to create audit logs send to monitoring service!!!"),
    FAIL_TO_LISTEN_USER_PATIENT_CREATED(HttpStatus.BAD_REQUEST, "Fail to listen user patient created!!"),
    MEDICAL_RECORD_DETAIL_NULL(HttpStatus.BAD_REQUEST, "Medical record id null!"),
    FAIL_TO_UPDATE_MEDICALRECORD(HttpStatus.BAD_REQUEST, "Error to update medical record because of an error"),
    PATIENT_FIND_WITH_SUB_NOT_FOUND(HttpStatus.BAD_REQUEST, "Patient find with sub not found"),
    FAIL_TO_PARSE_COMPLETED_AT(HttpStatus.BAD_REQUEST, "Error to parse completed at"),
    ;
    private final HttpStatusCode code;
    private final String message;

    ErrorCode(HttpStatusCode code, String message) {
        this.code = code;
        this.message = message;
    }
}
