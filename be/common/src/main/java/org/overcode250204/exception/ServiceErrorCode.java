package org.overcode250204.exception;

import org.springframework.http.HttpStatusCode;

public interface ServiceErrorCode {
    String name();
    String getMessage();
    HttpStatusCode getCode();
}