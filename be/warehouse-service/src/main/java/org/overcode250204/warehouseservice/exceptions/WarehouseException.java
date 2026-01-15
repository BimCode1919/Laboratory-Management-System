package org.overcode250204.warehouseservice.exceptions;

import org.overcode250204.exception.BaseException;



public class WarehouseException extends BaseException {
    public WarehouseException(ErrorCode errorCode) {
        super(errorCode);
    }
}
