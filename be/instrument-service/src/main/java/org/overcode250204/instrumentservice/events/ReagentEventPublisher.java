package org.overcode250204.instrumentservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.entity.OutboxEvent;
import org.overcode250204.instrumentservice.repository.OutboxRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReagentEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public String publishInstallRequest(String instrumentId, Map<String, Object> payload) {
        try {
            String eventId = createOutboxEvent("REAGENT_INSTALL_REQUEST", List.of(payload), instrumentId, "REAGENT");
            log.info("[Outbox] Created REAGENT_INSTALL_REQUEST event for instrumentId: {} eventId: {}", instrumentId, eventId);
            return eventId;
        } catch (Exception e) {
            log.error("Failed to create outbox event for REAGENT_INSTALL_REQUEST: {}", e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    public void publishUninstallRequest(String instrumentId, Map<String, Object> payload) {
        try {
            createOutboxEvent("REAGENT_UNINSTALL_REQUEST", List.of(payload), instrumentId, "REAGENT");
            log.info("[Outbox] Created REAGENT_UNINSTALL_REQUEST event for instrumentId: {}", instrumentId);
        } catch (Exception e) {
            log.error("Failed to create outbox event for REAGENT_UNINSTALL_REQUEST: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public String publishSyncRequest(String instrumentId, Map<String, Object> payload) {
        try {
            String eventId = createOutboxEvent("REAGENT_SYNC_REQUEST", List.of(payload), instrumentId, "REAGENT");
            log.info("[Outbox] Created REAGENT_SYNC_REQUEST event for instrumentId: {} eventId: {}", instrumentId, eventId);
            return eventId;
        } catch (Exception e) {
            log.error("Failed to create outbox event for REAGENT_SYNC_REQUEST: {}", e.getMessage(), e);
            return null;
        }
    }

    private String createOutboxEvent(String eventType, List<Map<String, Object>> payload, String aggregateId, String aggregateType) throws Exception {
        Map<String, Object> event = new HashMap<>();
        String eventId = UUID.randomUUID().toString();
        event.put("eventId", eventId);
        event.put("eventType", eventType);
        event.put("source", "instrument-service");
        event.put("timestamp", Instant.now().toString());
        event.put("payload", payload);

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType(aggregateType);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setStatus("PENDING");
        outboxEvent.setEventType(eventType);
        outboxEvent.setPayload(objectMapper.writeValueAsString(event));
        outboxEvent.setCreatedAt(Instant.now());
        outboxRepository.save(outboxEvent);
        return eventId;
    }
}
