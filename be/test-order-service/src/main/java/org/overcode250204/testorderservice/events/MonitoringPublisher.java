package org.overcode250204.testorderservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.models.entites.OutboxEvent;
import org.overcode250204.testorderservice.repositories.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonitoringPublisher {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.monitoring.logs:monitoring.logs.publish}")
    private String monitoringTopic;

    public void publishMonitoringEvent(String eventType, String aggregateId, Map<String, Object> payload) {

        try {
            Map<String, Object> envelope = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", eventType,
                    "source", "test-order-service",
                    "payload", payload,
                    "timestamp", Instant.now().toString()
            );

            OutboxEvent outboxLog = new OutboxEvent(
                    null,
                    monitoringTopic,
                    aggregateId,
                    eventType,
                    objectMapper.writeValueAsString(envelope),
                    "PENDING",
                    Instant.now()
            );

            outboxRepository.save(outboxLog);

        } catch (Exception e) {
            log.error("[Outbox Creator] Failed to queue monitoring event for type {}.", eventType, e);
            throw new RuntimeException("CRITICAL: Failed to create audit log, rolling back business transaction.", e);
        }

        log.info("[Outbox Creator] Monitoring event queued: type={} aggregateId={}", eventType, aggregateId);
    }
}