package org.overcode250204.testorderservice.services;

import org.overcode250204.testorderservice.dtos.HL7TestResult;
import org.overcode250204.testorderservice.models.entites.TestResultRaw;

import java.util.List;
import java.util.Map;

public interface TestResultRawService {
    List<TestResultRaw> saveRawTestResults(Map<String, Object> payload, HL7TestResult parsedResults);
}
