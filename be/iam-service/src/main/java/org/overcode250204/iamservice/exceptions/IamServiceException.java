package org.overcode250204.iamservice.exceptions;

import org.overcode250204.exception.BaseException;

public class IamServiceException extends BaseException {
    public IamServiceException(ErrorCode errorCode) {
        super(errorCode);
    }
}
