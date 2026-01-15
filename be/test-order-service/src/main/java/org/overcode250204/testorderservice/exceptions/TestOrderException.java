package org.overcode250204.testorderservice.exceptions;

import org.overcode250204.exception.BaseException;



public class TestOrderException extends BaseException {
    public TestOrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
