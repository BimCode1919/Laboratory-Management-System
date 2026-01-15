package org.overcode250204.instrumentservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.entity.OutboxEvent;
import org.overcode250204.instrumentservice.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    private Map<String, Object> wrapEvent(String type, Map<String, Object> payload) {
        Map<String, Object> message = new HashMap<>(payload);
        message.put("eventId", UUID.randomUUID().toString());
        message.put("eventType", type);
        message.put("timestamp", Instant.now().toString());
        message.put("source", "instrument-service");
        return message;
    }

    // ---- Publish request to analyzer adapter ----
    public void publishAnalysisRequest(String instrumentId, Map<String, Object> payload) {
        Map<String, Object> message = wrapEvent("ANALYSIS_REQUEST", payload);
        try {
            String payloadJson = objectMapper.writeValueAsString(message);
            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType("INSTRUMENT")
                    .aggregateId(instrumentId)
                    .eventType("ANALYSIS_REQUEST")
                    .payload(payloadJson)
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .build();

            outboxRepository.save(event);

            log.info("[OUTBOX] Stored ANALYSIS_REQUEST event for instrumentId={} barcode={}",
                    instrumentId, payload.get("barcode"));
        } catch (Exception e) {
            log.error("[OUTBOX] Failed to save ANALYSIS_REQUEST for instrumentId={}, cause={}",
                    instrumentId, e.getMessage(), e);
        }
    }
}
