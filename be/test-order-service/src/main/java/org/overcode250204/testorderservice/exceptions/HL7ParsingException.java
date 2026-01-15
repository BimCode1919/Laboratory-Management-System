package org.overcode250204.testorderservice.exceptions;

public class HL7ParsingException extends Exception{
    public HL7ParsingException(String message) {
        super(message);
    }

    public HL7ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}