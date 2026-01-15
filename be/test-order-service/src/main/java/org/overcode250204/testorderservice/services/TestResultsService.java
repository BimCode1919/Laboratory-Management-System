package org.overcode250204.testorderservice.services;

import org.overcode250204.testorderservice.dtos.TestResultTrendDTO;
import org.overcode250204.testorderservice.models.entites.TestResults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TestResultsService {
    List<TestResults> getByTestOrderId(UUID testOrderId);

    List<TestResultTrendDTO> getTrendResultsByPatientCode(
            UUID patientCode,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String parameterName
    );

    List<String> getAvailableParameterNamesByPatientCode(UUID patientCode);
}
