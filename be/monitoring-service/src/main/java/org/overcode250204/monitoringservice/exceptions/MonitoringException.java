package org.overcode250204.monitoringservice.exceptions;

import org.overcode250204.exception.BaseException;


public class MonitoringException extends BaseException {
    public MonitoringException(ErrorCode errorCode) {
        super(errorCode);
    }
}
