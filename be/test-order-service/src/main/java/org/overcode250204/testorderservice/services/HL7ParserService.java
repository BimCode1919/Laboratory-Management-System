package org.overcode250204.testorderservice.services;

import org.overcode250204.testorderservice.dtos.HL7TestResult;
import org.overcode250204.testorderservice.exceptions.HL7ParsingException;

public interface HL7ParserService {
    HL7TestResult parseHL7Message(String hl7Message) throws HL7ParsingException;
}