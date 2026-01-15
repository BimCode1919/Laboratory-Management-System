package org.overcode250204.patientservice.exceptions;

import org.overcode250204.exception.BaseException;



public class PatientException extends BaseException {
    public PatientException(ErrorCode errorCode) {
        super(errorCode);
    }
}
