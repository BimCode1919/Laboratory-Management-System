package org.overcode250204.instrumentservice.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.overcode250204.instrumentservice.dtos.ConfigurationDTO;
import org.overcode250204.instrumentservice.dtos.InstrumentSyncDTO;
import org.overcode250204.instrumentservice.entity.InboxEvent;
import org.overcode250204.instrumentservice.repository.InboxRepository;
import org.overcode250204.instrumentservice.service.interfaces.InstrumentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstrumentEventListener {

    private final InstrumentService instrumentService;
    private final ObjectMapper objectMapper;
    private final InboxRepository inboxRepository;

    @KafkaListener(
            topics = "${app.kafka.topics.configuration.sync.response:instrument.configuration.sync.response}",
            groupId = "instrument-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onConfigurationSyncResponse(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> event = record.value();
            String eventIdStr = (String) event.get("eventId");
            if (eventIdStr == null) {
                log.error("Event ID is missing, cannot process message from topic {}", record.topic());
                return;
            }
            UUID eventId = UUID.fromString(eventIdStr);

            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Event {} already processed, skipping.", eventId);
                return;
            }

            log.info("[Kafka] Received single instrument sync response: key={}, eventId={}", record.key(), eventId);

            // Extract the business payload from the standardized event structure
            List<Map<String, Object>> businessPayload = objectMapper.convertValue(event.get("payload"), new TypeReference<>() {});
            if (businessPayload == null || businessPayload.isEmpty()) {
                log.warn("Business payload is empty for single instrument sync response");
                return;
            }

            Map<String, Object> instrumentData = businessPayload.get(0);
            InstrumentSyncDTO instrumentSyncDTO = objectMapper.convertValue(instrumentData, InstrumentSyncDTO.class);

            // Debug: inspect configurations before saving
            if (instrumentSyncDTO.getConfigurations() != null) {
                log.debug("Converted InstrumentSyncDTO configurations size={} for instrumentId={}", instrumentSyncDTO.getConfigurations().size(), instrumentSyncDTO.getId());
                for (ConfigurationDTO c : instrumentSyncDTO.getConfigurations()) {
                    log.debug("Config item: configId={}, key={}, value={}", c.getConfigId(), c.getConfigKey(), c.getConfigValue());
                }
            } else {
                log.debug("No configurations present in converted InstrumentSyncDTO for instrumentId={}", instrumentSyncDTO.getId());
            }

            instrumentService.syncInstrument(instrumentSyncDTO);

            InboxEvent inboxEvent = new InboxEvent();
            inboxEvent.setEventId(eventId);
            inboxEvent.setPayload(objectMapper.writeValueAsString(event));
            inboxEvent.setProcessedAt(Instant.now());
            inboxRepository.save(inboxEvent);

            log.info("Successfully synced instrument ID: {} and marked event {} as processed", instrumentSyncDTO.getId(), eventId);
        } catch (Exception e) {
            log.error("Failed to process single instrument sync response: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "${app.kafka.topics.configuration.all.sync.response:instrument.configuration.all.sync.response}",
            groupId = "instrument-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onConfigurationAllSyncResponse(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> event = record.value();
            String eventIdStr = (String) event.get("eventId");
            if (eventIdStr == null) {
                log.error("Event ID is missing, cannot process message from topic {}", record.topic());
                return;
            }
            UUID eventId = UUID.fromString(eventIdStr);

            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Event {} already processed, skipping.", eventId);
                return;
            }

            log.info("[Kafka] Received all instruments sync response, eventId={}", eventId);

            // Extract the business payload from the standardized event structure
            List<Map<String, Object>> businessPayload = objectMapper.convertValue(event.get("payload"), new TypeReference<>() {});
            if (businessPayload == null) {
                log.warn("Business payload is missing for all instruments sync response");
                return;
            }

            log.info("[Kafka] Received all instruments sync response - items={}", businessPayload.size());
            for (Map<String, Object> instrumentData : businessPayload) {
                InstrumentSyncDTO instrumentSyncDTO = objectMapper.convertValue(instrumentData, InstrumentSyncDTO.class);

                if (instrumentSyncDTO.getConfigurations() != null) {
                    log.debug("Converted InstrumentSyncDTO configurations size={} for instrumentId={}", instrumentSyncDTO.getConfigurations().size(), instrumentSyncDTO.getId());
                } else {
                    log.debug("No configurations present in converted InstrumentSyncDTO for instrumentId={}", instrumentSyncDTO.getId());
                }

                instrumentService.syncInstrument(instrumentSyncDTO);
            }

            InboxEvent inboxEvent = new InboxEvent();
            inboxEvent.setEventId(eventId);
            inboxEvent.setPayload(objectMapper.writeValueAsString(event));
            inboxEvent.setProcessedAt(Instant.now());
            inboxRepository.save(inboxEvent);


            log.info("Successfully synced {} instruments from all-sync response and marked event {} as processed", businessPayload.size(), eventId);
        } catch (Exception e) {
            log.error("Failed to process all instruments sync response: {}", e.getMessage(), e);
        }
    }
}
