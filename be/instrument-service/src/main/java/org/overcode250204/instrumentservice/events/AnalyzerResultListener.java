package org.overcode250204.instrumentservice.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.entity.Instrument;
import org.overcode250204.instrumentservice.entity.InstrumentRun;
import org.overcode250204.instrumentservice.entity.OutboxEvent;
import org.overcode250204.instrumentservice.entity.RawTestResult;
import org.overcode250204.instrumentservice.enums.InstrumentRunStatus;
import org.overcode250204.instrumentservice.exception.ErrorCode;
import org.overcode250204.instrumentservice.exception.InstrumentException;
import org.overcode250204.instrumentservice.repository.InstrumentRepository;
import org.overcode250204.instrumentservice.repository.InstrumentRunRepository;
import org.overcode250204.instrumentservice.repository.OutboxRepository;
import org.overcode250204.instrumentservice.repository.RawTestResultRepository;
import org.overcode250204.instrumentservice.service.interfaces.EventLogService;
import org.overcode250204.instrumentservice.service.interfaces.HL7TemplateService;
import org.overcode250204.instrumentservice.service.interfaces.ReagentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyzerResultListener {

    private final ObjectMapper objectMapper;
    private final RawTestResultRepository rawTestResultRepository;
    private final InstrumentRunRepository instrumentRunRepository;
    private final InstrumentRepository instrumentRepository;

    private final HL7TemplateService hl7TemplateService;
    private final HL7Publisher hl7Publisher;
    private final SystemEventPublisher systemEventPublisher;
    private final EventLogService eventService;
    private final ReagentService reagentService;
    private final OutboxRepository outboxRepository;

    @KafkaListener(
            topics = "${app.kafka.topics.analysisResponse}",
            groupId = "instrument-service-result-consumers"
    )
    @Transactional
    public void onMessage(Map<String, Object> payload) {
        try {
            log.info("[AnalyzerResult] Received analyzer result payload: {}", payload);

            UUID runId = UUID.fromString((String) payload.get("runId"));
            UUID instrumentId = UUID.fromString((String) payload.get("instrumentId"));
            String barcode = (String) payload.get("barcode");
            String testType = (String) payload.get("testType");
            String status = (String) payload.getOrDefault("status", "FAILED");
            String userId = (String) payload.get("userId");
            Map<String, Object> rawData = (Map<String, Object>) payload.getOrDefault("rawData", Map.of());

            Instrument instrument = null;
            InstrumentRun run = instrumentRunRepository.findByRunId(runId).orElseThrow(() -> new InstrumentException(ErrorCode.RUN_NOT_FOUND));
            JsonNode reagentSnapshot = run.getReagentSnapshot();

            RawTestResult result = rawTestResultRepository.findByRunIdAndBarcode(runId, barcode)
                    .orElseGet(() -> {
                        RawTestResult r = new RawTestResult();
                        r.setRunId(runId);
                        r.setInstrumentId(instrumentId);
                        r.setBarcode(barcode);
                        r.setCreatedAt(LocalDateTime.now());
                        return r;
                    });

            if ("SUCCESS".equalsIgnoreCase(status)) {
                result.setStatus("COMPLETED");
                result.setCreatedBy(UUID.fromString(userId));
                // convert Map -> JsonNode (fix lỗi LinkedHashMap cannot be cast)
                JsonNode rawDataNode = objectMapper.valueToTree(rawData);
                result.setRawData(rawDataNode);
                result.setPublishedAt(LocalDateTime.now());

                // Build HL7 message
                instrument = instrumentRepository.findById(instrumentId)
                        .orElseThrow(() -> new InstrumentException(ErrorCode.INSTRUMENT_NOT_FOUND));
                String hl7Message = hl7TemplateService.buildOruMessage(instrument, barcode, testType, rawDataNode);
                result.setHl7Message(hl7Message);
                // --- Try publish HL7 messages ---
                Map<String, Object> eventPayload = Map.of();
                Map<String, Object> testResultPayload = Map.of();
                try {
                    eventPayload = hl7Publisher.publishRawBackup(instrumentId.toString(), runId, barcode, hl7Message);
                    String instrumentString = objectMapper.writeValueAsString(instrument);
                    testResultPayload = hl7Publisher.publishTestResult(instrumentId.toString(), barcode, hl7Message, runId, result.getCreatedBy(), reagentSnapshot.toString(), instrumentString);
                    result.setStatus("SENT");
                    log.info("[AnalyzerResult] HL7 published successfully for barcode={}", barcode);
                } catch (Exception ex) {
                    result.setStatus("FAILED");
                    result.setErrorMessage(ex.getMessage());
                    log.error("[AnalyzerResult] HL7 publish failed for barcode={}: {}", barcode, ex.getMessage());
                }

                outboxRepository.save(
                        OutboxEvent.builder()
                                .aggregateType("hl7.raw.backup")
                                .aggregateId(instrumentId.toString())
                                .eventType("HL7_RAW_BACKUP")
                                .payload(objectMapper.writeValueAsString(eventPayload))
                                .createdAt(Instant.now())
                                .build()
                );


                outboxRepository.save(
                        OutboxEvent.builder()
                                .aggregateType("hl7.testresult.publish")
                                .aggregateId(barcode)
                                .eventType("HL7_TEST_RESULT")
                                .payload(objectMapper.writeValueAsString(testResultPayload))
                                .createdAt(Instant.now())
                                .build()
                );

                log.info("[AnalyzerResult] HL7 published successfully for barcode={}", barcode);

                rawTestResultRepository.save(result);
                // Consume reagent
                if (testType != null) {
                    reagentService.consumeReagent(instrumentId, testType, 1);
                }

                eventService.logEvent(instrumentId, "SAMPLE_COMPLETED",
                        "Sample " + barcode + " completed successfully.", result.getCreatedBy());
            } else {
                result.setStatus("FAILED");
                result.setPublishedAt(LocalDateTime.now());
                rawTestResultRepository.save(result);

                eventService.logEvent(instrumentId, "SAMPLE_FAILED",
                        "Sample " + barcode + " failed during analysis.", result.getCreatedBy());
            }

            updateRunStatusAfterSample(runId, instrument);

        } catch (Exception e) {
            log.error("[AnalyzerResult] Error processing analyzer response: {}", e.getMessage(), e);
        }
    }


    private void updateRunStatusAfterSample(UUID runId, Instrument instrument) {
        InstrumentRun run = instrumentRunRepository.findByRunId(runId)
                .orElseThrow(() -> new InstrumentException(ErrorCode.RUN_NOT_FOUND));

        List<RawTestResult> results = rawTestResultRepository.findByRunId(runId);

        long successCount = results.stream()
                .filter(r -> "SENT".equals(r.getStatus()) || "COMPLETED".equals(r.getStatus()))
                .count();
        long failCount = results.stream()
                .filter(r -> "FAILED".equals(r.getStatus()))
                .count();

        run.setSuccessfulSamples((int) successCount);
        run.setFailedSamples((int) failCount);

        int expected = run.getTotalSamplesExpected();
        if (successCount + failCount >= expected) {
            run.setEndTime(LocalDateTime.now());

            if (successCount == expected) {
                run.setStatus(InstrumentRunStatus.COMPLETED);
            } else if (successCount > 0) {
                run.setStatus(InstrumentRunStatus.PARTIAL_COMPLETED);
            } else {
                run.setStatus(InstrumentRunStatus.FAILED);
            }

            instrumentRunRepository.save(run);

            systemEventPublisher.publishMonitoringEvent("INSTRUMENT_RUN_COMPLETION_LOG", Map.of(
                    "instrumentId", instrument.getId().toString(),
                    "runId", runId.toString(),
                    "success", successCount,
                    "failed", failCount
            ));

            systemEventPublisher.runInstrumentCompleted(Map.of(
                    "instrumentId", instrument.getId().toString(),
                    "instrument", instrument,
                    "runId", runId.toString(),
                    "success", successCount,
                    "failed", failCount
            ));

            eventService.logEvent(instrument.getId(), "RUN_COMPLETED",
                    "Run completed. Success=" + successCount + ", Fail=" + failCount, run.getCreatedBy());
        } else {
            instrumentRunRepository.save(run);
        }
    }

}
