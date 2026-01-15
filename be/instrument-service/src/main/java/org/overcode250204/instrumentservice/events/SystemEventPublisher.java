package org.overcode250204.instrumentservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.entity.OutboxEvent;
import org.overcode250204.instrumentservice.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemEventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.monitoring:instrument.monitoring}")
    private String monitoringTopic;

    @Value("${app.kafka.topics.configSync:instrument.config.sync}")
    private String configSyncTopic;

    @Value("${app.kafka.topics.runCompleted}")
    private String runCompletedTopic;

    public void publishMonitoringEvent(String eventType, Map<String, Object> payload) {

        try {
            Map<String, Object> envelope = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", eventType,
                    "source", "instrument-service",
                    "payload", payload,
                    "timestamp", Instant.now().toString()
            );

            outboxRepository.save(
                    OutboxEvent.builder()
                            .aggregateType(monitoringTopic)
                            .aggregateId(eventType)
                            .eventType(eventType)
                            .payload(objectMapper.writeValueAsString(envelope))
                            .createdAt(Instant.now())
                            .status("PENDING")
                            .build()
            );
        } catch (Exception e) {
            log.error("[Outbox] Failed to queue monitoring event: {}", e.getMessage(), e);
        }

        log.info("[Outbox] Monitoring event queued: type={} payload={}", eventType, payload);
    }

    public void runInstrumentCompleted(Map<String, Object> payload) {
        try {
            Map<String, Object> envelope = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", "INSTRUMENT_RUN_COMPLETED",
                    "source", "instrument-service",
                    "payload", payload,
                    "timestamp", Instant.now().toString()
            );

            outboxRepository.save(
                    OutboxEvent.builder()
                            .aggregateType(runCompletedTopic)
                            .aggregateId((String) payload.get("runId"))
                            .eventType("INSTRUMENT_RUN_COMPLETED")
                            .payload(objectMapper.writeValueAsString(envelope))
                            .createdAt(Instant.now())
                            .status("PENDING")
                            .build()
            );

            log.info("[Outbox] RunCompleted event queued: {}", payload);

        } catch (Exception e) {
            log.error("[Outbox] Failed to queue runCompleted event: {}", e.getMessage(), e);
        }
    }

}
