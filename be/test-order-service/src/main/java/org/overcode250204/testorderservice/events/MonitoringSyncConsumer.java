package org.overcode250204.testorderservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.dtos.HL7TestResult;
import org.overcode250204.testorderservice.exceptions.ErrorCode;
import org.overcode250204.testorderservice.exceptions.TestOrderException;
import org.overcode250204.testorderservice.models.entites.InboxEvent;
import org.overcode250204.testorderservice.models.entites.TestResultRaw;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.overcode250204.testorderservice.repositories.InboxRepository;
import org.overcode250204.testorderservice.services.HL7ParserService;
import org.overcode250204.testorderservice.services.TestResultProcessingService;
import org.overcode250204.testorderservice.services.TestResultRawService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringSyncConsumer {

    private final ObjectMapper objectMapper;
    private final InboxRepository inboxRepository;

    private final HL7ParserService hl7ParserService;
    private final TestResultRawService testResultRawService;
    private final TestResultProcessingService testResultProcessingService;

    @KafkaListener(
            topics = "monitoring.logs.publish",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void onMonitoringSyncEvent(Map<String, Object> message) {
        try {
            String eventType = (String) message.get("eventType");
            if (eventType == null) {
                log.warn("[TestOrder] Received message without eventType: {}", message);
                return;
            }

            log.info("[TestOrder] Monitoring event received: {}", eventType);

            switch (eventType) {
                case "RAW_RESULT_SYNCED" -> handleSynced(message);
                case "RAW_RESULT_NOT_FOUND" -> handleNotFound(message);
                case "RAW_RESULT_SYNC_FAILED" -> handleFailed(message);
                default -> log.warn("[TestOrder] Unsupported eventType: {}", eventType);
            }

        } catch (Exception ex) {
            log.error("[TestOrder] Error handling monitoring sync event", ex);
            throw new TestOrderException(ErrorCode.FAIL_TO_LISTEN_HL7_TEST_RESULT_PUBLISH);
        }
    }

    // --------------------------------------------------------------------
    // HANDLERS
    // --------------------------------------------------------------------

    private void handleSynced(Map<String, Object> message) throws Exception {
        UUID eventId = extractEventId(message);
        if (isProcessed(eventId)) return;

        Map<String, Object> payload = extractPayload(message);
        String barcode = (String) payload.get("barcode");
        String hl7Message = (String) payload.get("hl7Message");

        if (hl7Message == null)
            throw new TestOrderException(ErrorCode.HL7_PARSING_FAILED);

        HL7TestResult parsed = hl7ParserService.parseHL7Message(hl7Message);

        List<TestResultRaw> raws = testResultRawService.saveRawTestResults(payload, parsed);

        List<TestResults> processed = testResultProcessingService.processRawResults(
                raws.stream().map(TestResultRaw::getRunId).toList()
        );

        log.info("[TestOrder] RAW_RESULT_SYNCED OK | barcode={} | normalized={}", barcode, processed.size());
    }

    private void handleNotFound(Map<String, Object> message) throws Exception {
        UUID eventId = extractEventId(message);
        if (isProcessed(eventId)) return;

        Map<String, Object> payload = extractPayload(message);
        String barcode = (String) payload.get("barcode");

        log.warn("[TestOrder] RAW_RESULT_NOT_FOUND | barcode={}", barcode);

        testResultProcessingService.markWaitingForInstrument(barcode);
    }

    private void handleFailed(Map<String, Object> message) throws Exception {
        UUID eventId = extractEventId(message);
        if (isProcessed(eventId)) return;

        Map<String, Object> payload = extractPayload(message);
        String barcode = (String) payload.get("barcode");

        log.error("[TestOrder] RAW_RESULT_SYNC_FAILED | barcode={}", barcode);

        testResultProcessingService.markSyncFailed(barcode);
    }

    // --------------------------------------------------------------------
    // SHARED UTILITIES
    // --------------------------------------------------------------------

    private UUID extractEventId(Map<String, Object> message) {
        String id = (String) message.get("eventId");
        if (id == null)
            throw new TestOrderException(ErrorCode.EVENT_ID_MISSING);
        return UUID.fromString(id);
    }

    private Map<String, Object> extractPayload(Map<String, Object> message) {
        return (Map<String, Object>) message.get("payload");
    }

    private boolean isProcessed(UUID eventId) throws Exception {
        if (inboxRepository.existsByEventId(eventId)) {
            log.info("[TestOrder] Event {} already processed", eventId);
            return true;
        }

        InboxEvent inbox = new InboxEvent();
        inbox.setEventId(eventId);
        inbox.setPayload(objectMapper.writeValueAsString(Map.of("eventId", eventId)));
        inbox.setProcessedAt(Instant.now());

        inboxRepository.save(inbox);
        return false;
    }
}
