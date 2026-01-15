package org.overcode250204.instrumentservice.service.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.overcode250204.instrumentservice.dtos.AnalyzeCommand;
import org.overcode250204.instrumentservice.dtos.AnalysisResultDTO;
import org.overcode250204.instrumentservice.dtos.ReagentEstimateDTO;

import java.util.List;
import java.util.UUID;

public interface BloodAnalysisService {

    AnalysisResultDTO analyze(UUID instrumentId, AnalyzeCommand req) throws JsonProcessingException;

    void finishRun(UUID runId, UUID userId);

    AnalysisResultDTO getRun(UUID runId);

    List<AnalysisResultDTO> getRunsByInstrument(UUID instrumentId);

    ReagentEstimateDTO estimateReagentRequirement(UUID instrumentId, AnalyzeCommand req);

}
