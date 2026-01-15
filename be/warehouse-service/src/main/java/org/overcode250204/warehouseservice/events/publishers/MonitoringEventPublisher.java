package org.overcode250204.warehouseservice.events.publishers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.warehouseservice.events.OutboxEvent;
import org.overcode250204.warehouseservice.repositories.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonitoringEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.kafka.topics.monitoring}")
    private String monitoringTopic;

    private Map<String, Object> wrapEvent(String type, Map<String, Object> payload) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventId", UUID.randomUUID().toString());
        message.put("eventType", type);
        message.put("timestamp", Instant.now().toString());
        message.put("source", "warehouse-service");
        message.put("payload", payload);
        return message;
    }

    public void publishEvent(String entityType, String entityId, String eventType, Map<String, Object> payload) {
        Map<String, Object> message = wrapEvent(eventType, payload);

        try {
            OutboxEvent event = new OutboxEvent();
            event.setAggregateType(entityType);
            event.setAggregateId(entityId);
            event.setEventType(eventType);
            event.setPayload(objectMapper.writeValueAsString(message));
            event.setCreatedAt(Instant.now());
            outboxRepository.save(event);

            log.info("[Outbox] Stored Monitoring event: type={} topic={}", eventType, monitoringTopic);
        } catch (Exception e) {
            log.error("Failed to store Monitoring event in outbox: {}", e.getMessage(), e);
        }
    }
}
