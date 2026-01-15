package org.overcode250204.testorderservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.testorderservice.dtos.TestResultTrendDTO;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.overcode250204.testorderservice.services.TestResultsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/test-results")
@RequiredArgsConstructor
public class TestResultController {
    private final TestResultsService testResultsService;
    private static final String SERVICE_NAME = "TestOrderService";

    @GetMapping("/{testOrderId}")
    public ResponseEntity<?> getByTestOrderId(
            @PathVariable UUID testOrderId
    ) {
        List<TestResults> testResults = testResultsService.getByTestOrderId(testOrderId);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, testResults));
    }

    @GetMapping("/patients/{patientCode}/trend")
    public ResponseEntity<?> getTrendResultsByPatientCode(
            @PathVariable UUID patientCode,
            @RequestParam(name = "startTime")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(name = "endTime")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(name = "parameterName") String parameterName
    ) {
        List<TestResultTrendDTO> trendResults = testResultsService.getTrendResultsByPatientCode(
                patientCode,
                startTime,
                endTime,
                parameterName
        );
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, trendResults));
    }

    @GetMapping("/patients/{patientId}/parameters")
    public ResponseEntity<?> getAvailableParameterNames(
            @PathVariable UUID patientId
    ) {
        List<String> parameterNames = testResultsService.getAvailableParameterNamesByPatientCode(patientId);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, parameterNames));
    }
}
