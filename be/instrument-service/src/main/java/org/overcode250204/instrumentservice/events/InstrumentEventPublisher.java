package org.overcode250204.instrumentservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.entity.OutboxEvent;
import org.overcode250204.instrumentservice.repository.OutboxRepository;
import org.overcode250204.instrumentservice.utils.AuthUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public String publishConfigurationSyncRequest(String instrumentId) {
        try {
            Map<String, Object> businessPayload = new HashMap<>();
            businessPayload.put("instrumentId", instrumentId);
            UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
            businessPayload.put("userId", userId);

            String eventId = createOutboxEvent(
                    "CONFIGURATION_SYNC_REQUEST",
                    List.of(businessPayload),
                    instrumentId,
                    "INSTRUMENT"
            );

            log.info("Created outbox event for CONFIGURATION_SYNC_REQUEST with aggregateId {} eventId {}", instrumentId, eventId);
            return eventId;
        } catch (Exception e) {
            log.error("Failed to create outbox event for CONFIGURATION_SYNC_REQUEST: {}", e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    public String publishConfigurationAllSyncRequest() {
        try {
            Map<String, Object> businessPayload = new HashMap<>();
            UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
            businessPayload.put("userId", userId);

            String eventId = createOutboxEvent(
                    "CONFIGURATION_ALL_SYNC_REQUEST",
                    List.of(businessPayload),
                    userId.toString(),
                    "INSTRUMENT_ALL"
            );

            log.info("Created outbox event for CONFIGURATION_ALL_SYNC_REQUEST eventId {}", eventId);
            return eventId;
        } catch (Exception e) {
            log.error("Failed to create outbox event for CONFIGURATION_ALL_SYNC_REQUEST: {}", e.getMessage(), e);
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
