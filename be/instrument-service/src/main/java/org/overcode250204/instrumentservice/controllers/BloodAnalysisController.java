package org.overcode250204.instrumentservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.instrumentservice.dtos.AnalyzeCommand;
import org.overcode250204.instrumentservice.dtos.AnalysisResultDTO;
import org.overcode250204.instrumentservice.dtos.ReagentEstimateDTO;
import org.overcode250204.instrumentservice.service.interfaces.BloodAnalysisService;
import org.overcode250204.instrumentservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/instruments")
@RequiredArgsConstructor
@Slf4j
public class BloodAnalysisController {

    @Value("${service.application.name:instrument-service}")
    private String serviceName;

    private final BloodAnalysisService bloodAnalysisService;


    @PostMapping("/{instrumentId}/analysis/start")
    @PreAuthorize("hasAuthority('TEST_EXECUTE')")
    public ResponseEntity<BaseResponse<AnalysisResultDTO>> startAnalysis(
            @PathVariable UUID instrumentId,
            @RequestBody AnalyzeCommand request) throws JsonProcessingException {

        UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        request.setUserId(userId);
        AnalysisResultDTO response = bloodAnalysisService.analyze(instrumentId, request);
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }


    @PostMapping("/analysis/finish/{runId}")
    @PreAuthorize("hasAuthority('TEST_EXECUTE')")
    public ResponseEntity<BaseResponse<Void>> finishRun(@PathVariable UUID runId) {
        UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());

        bloodAnalysisService.finishRun(runId, userId);
        return ResponseEntity.ok(BaseResponse.success(serviceName, null));
    }


    @GetMapping("/analysis/runs/{runId}")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<AnalysisResultDTO>> getRun(
            @PathVariable UUID runId) {

        AnalysisResultDTO run = bloodAnalysisService.getRun(runId);
        return ResponseEntity.ok(BaseResponse.success(serviceName, run));
    }

    @GetMapping("/{instrumentId}/analysis/runs")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<List<AnalysisResultDTO>>> getAllRuns(
            @PathVariable UUID instrumentId) {

        List<AnalysisResultDTO> runs = bloodAnalysisService.getRunsByInstrument(instrumentId);
        return ResponseEntity.ok(BaseResponse.success(serviceName, runs));
    }

    @PostMapping("/{instrumentId}/analysis/estimate")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<ReagentEstimateDTO>> estimateReagents(@PathVariable UUID instrumentId,
                                                                             @RequestBody AnalyzeCommand request) {
        UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        request.setUserId(userId);
        ReagentEstimateDTO estimate = bloodAnalysisService.estimateReagentRequirement(instrumentId, request);
        return ResponseEntity.ok(BaseResponse.success(serviceName, estimate));
    }
}
