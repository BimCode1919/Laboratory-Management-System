package org.overcode250204.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {

    private final ServiceErrorCode errorCode;

    public BaseException(ServiceErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
