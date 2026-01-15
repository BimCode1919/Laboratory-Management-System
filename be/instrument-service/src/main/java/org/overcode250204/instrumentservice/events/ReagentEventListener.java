package org.overcode250204.instrumentservice.events;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.overcode250204.instrumentservice.entity.InboxEvent;
import org.overcode250204.instrumentservice.entity.InstalledReagent;
import org.overcode250204.instrumentservice.entity.Instrument;
import org.overcode250204.instrumentservice.exception.ErrorCode;
import org.overcode250204.instrumentservice.exception.InstrumentException;
import org.overcode250204.instrumentservice.repository.InboxRepository;
import org.overcode250204.instrumentservice.repository.InstalledReagentRepository;
import org.overcode250204.instrumentservice.repository.InstrumentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReagentEventListener {
    private final InstalledReagentRepository reagentRepository;
    private final InstrumentRepository instrumentRepository;
    private final InboxRepository inboxRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @KafkaListener(
            topics = "${app.kafka.topics.reagent.install:warehouse.reagent.install.response}",
            groupId = "instrument-service-reagent",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onReagentInstall(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> event = record.value();
            UUID eventId = UUID.fromString((String) event.get("eventId"));

            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Reagent install event {} already processed, skipping.", eventId);
                return;
            }

            log.info("Received reagent install event: {}", event);

            List<Map<String, Object>> payload = objectMapper.convertValue(event.get("payload"), new TypeReference<>() {});
            if (payload == null || payload.isEmpty()) {
                log.warn("Payload is empty for reagent install event");
                return;
            }

            processReagentPayload(payload.get(0), false);

            saveInboxEvent(eventId, event);

        } catch (Exception e) {
            log.error("Failed to process reagent install event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "${app.kafka.topics.reagent.sync.response:warehouse.reagent.sync.response}",
            groupId = "instrument-service-reagent",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onReagentSync(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> event = record.value();
            UUID eventId = UUID.fromString((String) event.get("eventId"));

            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Reagent sync event {} already processed, skipping.", eventId);
                return;
            }

            log.info("Received reagent sync event: {}", event);

            List<Map<String, Object>> payload = objectMapper.convertValue(event.get("payload"), new TypeReference<>() {});
            if (payload == null) { // Empty list is a valid case for sync
                log.warn("Payload is missing for reagent sync event");
                return;
            }

            if (payload.isEmpty()) {
                log.info("Reagent sync event received with empty payload. No reagents to process.");
                saveInboxEvent(eventId, event);
                return;
            }

            for (Map<String, Object> item : payload) {
                processReagentPayload(item, true); // isSync=true to replace existing
            }

            saveInboxEvent(eventId, event);

        } catch (Exception e) {
            log.error("Failed to process reagent sync event: {}", e.getMessage(), e);
        }
    }

    private void processReagentPayload(Map<String, Object> payload, boolean isSync) {
        if ("FAIL".equalsIgnoreCase((String) payload.get("status"))) {
            log.warn("Reagent operation failed from warehouse: reason={}", payload.get("reason"));
            return;
        }

        String instrumentIdStr = (String) payload.get("instrumentId");
        if (instrumentIdStr == null) {
            log.warn("Event payload missing instrumentId: {}", payload);
            return;
        }

        UUID instrumentId = UUID.fromString(instrumentIdStr);
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new InstrumentException(ErrorCode.INSTRUMENT_NOT_FOUND));

        String reagentName = (String) payload.get("reagentName");
        String lotNumber = (String) payload.get("lotNumber");

        InstalledReagent reagent;
        if (isSync) {
            reagent = new InstalledReagent();
        } else {
            Optional<InstalledReagent> existing = reagentRepository.findByInstrumentIdAndReagentNameAndLotNumber(
                    instrumentId, reagentName, lotNumber);
            reagent = existing.orElseGet(InstalledReagent::new);
        }

        double installedQuantity = ((Number) payload.getOrDefault("quantityRemaining", 0)).doubleValue();
        double currentQuantity = (reagent.getQuantityRemaining() != null && !isSync) ? reagent.getQuantityRemaining() : 0.0;
        double newQuantity = currentQuantity + installedQuantity;

        reagent.setInstrument(instrument);
        reagent.setReagentId(UUID.fromString((String) payload.get("reagentId")));
        reagent.setReagentName(reagentName);
        reagent.setLotNumber(lotNumber);
        reagent.setVendorName((String) payload.get("vendorName"));
        if (payload.get("expirationDate") != null) {
            reagent.setExpirationDate(objectMapper.convertValue(payload.get("expirationDate"), LocalDate.class));
        }
        reagent.setQuantityRemaining(newQuantity);
        reagent.setUnit((String) payload.get("unit"));
        reagent.setStatus((String) payload.get("status"));
        if (payload.get("installedAt") != null) {
            reagent.setInstalledAt(objectMapper.convertValue(payload.get("installedAt"), LocalDateTime.class));
        }
        if (payload.get("installedBy") != null) {
            try {
                reagent.setInstalledBy(UUID.fromString((String) payload.get("installedBy")));
            } catch (Exception ex) {
                log.warn("installedBy is not a valid UUID: {}", payload.get("installedBy"));
            }
        }
        if (payload.get("lastCheckedAt") != null) {
            reagent.setLastCheckedAt(objectMapper.convertValue(payload.get("lastCheckedAt"), LocalDateTime.class));
        }
        reagent.setInUse(true);
        reagent.setUninstalledAt(null);

        reagentRepository.save(reagent);
        log.info("Processed reagent [{} - {}] for instrument {}. New quantity: {}", reagent.getReagentName(), reagent.getLotNumber(), instrumentId, newQuantity);
    }

    private void saveInboxEvent(UUID eventId, Map<String, Object> event) throws Exception {
        InboxEvent inboxEvent = new InboxEvent();
        inboxEvent.setEventId(eventId);
        inboxEvent.setPayload(objectMapper.writeValueAsString(event));
        inboxEvent.setProcessedAt(Instant.now());
        inboxRepository.save(inboxEvent);
        log.info("Marked event {} as processed.", eventId);
    }
}
