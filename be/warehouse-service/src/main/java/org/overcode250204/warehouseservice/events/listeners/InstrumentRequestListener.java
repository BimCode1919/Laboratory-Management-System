package org.overcode250204.warehouseservice.events.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.overcode250204.warehouseservice.events.InboxEvent;
import org.overcode250204.warehouseservice.events.OutboxEvent;
import org.overcode250204.warehouseservice.model.entities.Configuration;
import org.overcode250204.warehouseservice.model.entities.Instrument;
import org.overcode250204.warehouseservice.repositories.InboxRepository;
import org.overcode250204.warehouseservice.repositories.InstrumentsRepository;
import org.overcode250204.warehouseservice.repositories.OutboxRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentRequestListener {

    private final InstrumentsRepository instrumentsRepository;
    private final InboxRepository inboxRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.configuration.sync.request:instrument.configuration.sync.request}", groupId = "warehouse-service-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onConfigurationSyncRequest(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> event = record.value();
            String eventIdStr = (String) event.get("eventId");
            if (eventIdStr == null) {
                log.error("Event ID is missing, cannot process message from topic {}", record.topic());
                return;
            }
            UUID eventId = UUID.fromString(eventIdStr);

            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Request event {} already processed, skipping.", eventId);
                return;
            }

            log.info("[Kafka] Received configuration sync request: eventId={}", eventId);

            // Correctly extract the business payload
            List<Map<String, Object>> outerPayload = (List<Map<String, Object>>) event.get("payload");
            if (outerPayload == null || outerPayload.isEmpty()) {
                log.warn("Payload is empty for configuration sync request");
                return;
            }
            Map<String, Object> businessPayload = outerPayload.get(0);
            String instrumentIdStr = (String) businessPayload.get("instrumentId");

            if (instrumentIdStr == null) {
                log.warn("Missing instrumentId in configuration sync request: {}", businessPayload);
                return;
            }

            UUID instrumentId = UUID.fromString(instrumentIdStr);
            Optional<Instrument> instrumentOpt = instrumentsRepository.findInstrumentsByInstrumentId(instrumentId);

            if (instrumentOpt.isPresent()) {
                Instrument instrument = instrumentOpt.get();
                Map<String, Object> responsePayload = mapInstrumentToResponse(instrument);

                createOutboxEvent(
                        "CONFIGURATION_SYNC_RESPONSE",
                        List.of(responsePayload),
                        instrument.getInstrumentId().toString(),
                        "INSTRUMENT_CONFIG",
                        eventId
                );
                log.info("Created outbox event for CONFIGURATION_SYNC_RESPONSE for instrumentId: {}", instrument.getInstrumentId());
            } else {
                log.warn("[Kafka] Instrument not found for id: {}", instrumentIdStr);
            }

            // Mark event as processed
            InboxEvent inboxEvent = new InboxEvent();
            inboxEvent.setEventId(eventId);
            inboxEvent.setPayload(objectMapper.writeValueAsString(event));
            inboxEvent.setProcessedAt(Instant.now());
            inboxRepository.save(inboxEvent);

        } catch (Exception e) {
            log.error("Failed to process configuration sync request: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.configuration.all.sync.request:instrument.configuration.all.sync.request}", groupId = "warehouse-service-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onConfigurationAllSyncRequest(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> event = record.value();
            String eventIdStr = (String) event.get("eventId");
            if (eventIdStr == null) {
                log.error("Event ID is missing, cannot process message from topic {}", record.topic());
                return;
            }
            UUID eventId = UUID.fromString(eventIdStr);

            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Request event {} already processed, skipping.", eventId);
                return;
            }

            log.info("[Kafka] Received configuration all sync request: eventId={}", eventId);

            // The business payload for all sync request is the list of instruments itself
            List<Map<String, Object>> businessPayload = (List<Map<String, Object>>) event.get("payload");
            if (businessPayload == null) {
                log.warn("Payload is missing for all instruments sync response");
                return;
            }

            List<Instrument> instruments = instrumentsRepository.findAll();

            List<Map<String, Object>> responsePayload = instruments.stream()
                    .map(this::mapInstrumentToResponse)
                    .collect(Collectors.toList());

            createOutboxEvent(
                    "CONFIGURATION_ALL_SYNC_RESPONSE",
                    responsePayload,
                    "ALL_INSTRUMENTS",
                    "INSTRUMENT_CONFIG",
                    eventId
            );
            log.info("Created outbox event for CONFIGURATION_ALL_SYNC_RESPONSE with {} instruments.", instruments.size());

            // Mark event as processed
            InboxEvent inboxEvent = new InboxEvent();
            inboxEvent.setEventId(eventId);
            inboxEvent.setPayload(objectMapper.writeValueAsString(event));
            inboxEvent.setProcessedAt(Instant.now());
            inboxRepository.save(inboxEvent);

        } catch (Exception e) {
            log.error("Failed to process configuration all sync request: {}", e.getMessage(), e);
        }
    }

    private void createOutboxEvent(String eventType, List<Map<String, Object>> payload, String aggregateId, String aggregateType, UUID originalEventId) throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", originalEventId.toString()); // Include original event ID for tracing
        event.put("eventType", eventType);
        event.put("source", "warehouse-service");
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
    }

    private Map<String, Object> mapInstrumentToResponse(Instrument instrument) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", instrument.getInstrumentId() != null ? instrument.getInstrumentId().toString() : null);
        response.put("instrumentCode", instrument.getInstrumentCode());
        response.put("name", instrument.getName());
        response.put("model", instrument.getModel());
        response.put("serialNumber", instrument.getSerialNumber());
        response.put("location", instrument.getLocation());
        response.put("status", instrument.getStatus() != null ? instrument.getStatus().name() : null);
        // mode should be separate enum; fallback to null if not available
        response.put("mode", instrument.getStatus() != null ? instrument.getStatus().name() : null);
        response.put("lastModeChangeAt", null);
        response.put("createdBy", instrument.getCreatedBy() != null ? instrument.getCreatedBy().toString() : null);
        response.put("createdAt", instrument.getCreatedAt() != null ? instrument.getCreatedAt().toString() : null);
        response.put("updatedAt", instrument.getUpdatedAt() != null ? instrument.getUpdatedAt().toString() : null);
        response.put("configVersion", instrument.getConfigVersion());
        response.put("lastConfigSyncAt", null);
        response.put("isOnline", null);

        // Map configurations (warehouse stores configuration as separate entity)
        List<Map<String, Object>> configs = new ArrayList<>();
        if (instrument.getConfigurations() != null) {
            for (Configuration cfg : instrument.getConfigurations()) {
                Map<String, Object> c = new HashMap<>();
                c.put("configId", cfg.getConfigId() != null ? cfg.getConfigId().toString() : null);
                c.put("configName", cfg.getConfigName());
                c.put("configKey", cfg.getConfigKey());
                c.put("configValue", cfg.getConfigValue());
                c.put("defaultValue", cfg.getDefaultValue());
                c.put("description", cfg.getDescription());
                c.put("isGlobal", cfg.getIsGlobal() != null ? cfg.getIsGlobal() : false);
                c.put("createdAt", cfg.getCreatedAt() != null ? cfg.getCreatedAt().toString() : null);
                c.put("updatedAt", cfg.getUpdatedAt() != null ? cfg.getUpdatedAt().toString() : null);
                c.put("createdBy", cfg.getCreatedBy() != null ? cfg.getCreatedBy().toString() : null);
                c.put("updatedBy", cfg.getUpdatedBy() != null ? cfg.getUpdatedBy().toString() : null);
                configs.add(c);
            }
        }
        response.put("configurations", configs);

        return response;
    }
}