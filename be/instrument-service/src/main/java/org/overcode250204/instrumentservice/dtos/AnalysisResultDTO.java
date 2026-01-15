package org.overcode250204.instrumentservice.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.overcode250204.instrumentservice.enums.InstrumentRunStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResultDTO {

    private UUID runId;
    private UUID instrumentId;
    private String instrumentName;
    private InstrumentRunStatus status;
    private Integer totalSamplesExpected;
    private Integer successfulSamples;
    private Integer failedSamples;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private UUID createdBy;
    private JsonNode reagentSnapshot;
    private List<RawSampleResult> results;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RawSampleResult {
        private String barcode;
        private String status;
        private JsonNode rawData;
    }
}

