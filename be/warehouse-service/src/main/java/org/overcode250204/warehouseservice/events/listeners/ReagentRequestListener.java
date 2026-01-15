package org.overcode250204.warehouseservice.events.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.overcode250204.warehouseservice.events.InboxEvent;
import org.overcode250204.warehouseservice.events.OutboxEvent;
import org.overcode250204.warehouseservice.model.entities.*;
import org.overcode250204.warehouseservice.repositories.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReagentRequestListener {

    private final ReagentsRepository reagentsRepository;
    private final ReagentSupplyHistoryRepository supplyHistoryRepository;
    private final ReagentUsageHistoryRepository usageHistoryRepository;
    private final InstrumentsRepository instrumentRepository;
    private final InstrumentReagentsRepository instrumentReagentsRepository;
    private final InboxRepository inboxRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.reagent.install.request:warehouse.reagent.install.request}", groupId = "warehouse-service-reagent", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onInstallRequest(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> event = record.value();
            UUID eventId = UUID.fromString((String) event.get("eventId"));

            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Reagent install request {} already processed, skipping.", eventId);
                return;
            }

            log.info("[Kafka] Received install request: {}", event);

            List<Map<String, Object>> payload = (List<Map<String, Object>>) event.get("payload");
            if (payload == null || payload.isEmpty()) {
                log.warn("Payload is empty for install request");
                return;
            }

            Map<String, Object> requestPayload = payload.get(0);
            String instrumentId = (String) requestPayload.get("instrumentId");
            String reagentIdStr = (String) requestPayload.get("reagentId");
            UUID reagentId = UUID.fromString(reagentIdStr);
            String installedBy = (String) requestPayload.get("installedBy");
            Number quantityRequestedNumber = (Number) requestPayload.get("quantity");
            BigDecimal quantityRequested = (quantityRequestedNumber != null) ? new BigDecimal(quantityRequestedNumber.toString()) : BigDecimal.ZERO;

            Map<String, Object> responsePayload = new HashMap<>();
            responsePayload.put("instrumentId", instrumentId);
            responsePayload.put("reagentId", reagentIdStr);

            UUID instrumentUuid;
            try {
                instrumentUuid = UUID.fromString(instrumentId);
            } catch (Exception ex) {
                responsePayload.put("status", "FAIL");
                responsePayload.put("reason", "Invalid instrumentId");
                createOutboxEvent("REAGENT_INSTALL_RESPONSE", List.of(responsePayload), instrumentId, "REAGENT_RESPONSE", eventId);
                log.warn("[Kafka] Install failed - invalid instrumentId: {}", instrumentId);
                saveInboxEvent(eventId, event);
                return;
            }

            if (instrumentRepository.findById(instrumentUuid).isEmpty()) {
                responsePayload.put("status", "FAIL");
                responsePayload.put("reason", "Instrument not found");
                createOutboxEvent("REAGENT_INSTALL_RESPONSE", List.of(responsePayload), instrumentId, "REAGENT_RESPONSE", eventId);
                log.warn("[Kafka] Install failed - instrument not found: {}", instrumentId);
                saveInboxEvent(eventId, event);
                return;
            }

            Optional<Reagent> found = reagentsRepository.findById(reagentId);
            if (found.isPresent()) {
                Reagent r = found.get();
                BigDecimal totalSupply = supplyHistoryRepository.calculateTotalSupplyByReagentId(r.getReagentId());
                BigDecimal totalUsage = usageHistoryRepository.calculateTotalUsageByReagentId(r.getReagentId());
                BigDecimal availableQuantity = totalSupply.subtract(totalUsage);

                if (availableQuantity.compareTo(quantityRequested) >= 0) {
                    ReagentUsageHistory usage = new ReagentUsageHistory();
                    usage.setReagent(r);
                    usage.setQuantityUsed(quantityRequested);
                    usage.setUsageDate(LocalDateTime.now());
                    usage.setUsedBy(installedBy);
                    usage.setAction("INSTALL");
                    usage.setNote("Installed on instrument " + instrumentId);
                    usageHistoryRepository.save(usage);

                    ReagentSupplyHistory supplyHistory = supplyHistoryRepository.findReagentSupplyHistoriesByReagent(r);
                    responsePayload.put("reagentName", r.getName().toUpperCase());
                    responsePayload.put("lotNumber", supplyHistory.getLotNumber());
                    responsePayload.put("vendorName", "demo");
                    responsePayload.put("expirationDate", supplyHistory.getExpirationDate());
                    responsePayload.put("quantityRemaining", quantityRequested);
                    responsePayload.put("unit", supplyHistory.getUnitOfMeasure());
                    responsePayload.put("status", "INSTALLED");
                    responsePayload.put("installedAt", LocalDateTime.now());
                    responsePayload.put("installedBy", installedBy);
                    responsePayload.put("lastCheckedAt", LocalDateTime.now());
                    responsePayload.put("inUse", true);
                    responsePayload.put("removedBy", null);
                    responsePayload.put("uninstalledAt", null);

                    createOutboxEvent("REAGENT_INSTALL_RESPONSE", List.of(responsePayload), instrumentId, "REAGENT_RESPONSE", eventId);
                    log.info("[Kafka] Sent install success for reagent={} instrument={}", r.getName(), instrumentId);
                } else {
                    responsePayload.put("status", "FAIL");
                    responsePayload.put("reason", "Insufficient quantity in warehouse. Available: " + availableQuantity + ", Requested: " + quantityRequested);
                    createOutboxEvent("REAGENT_INSTALL_RESPONSE", List.of(responsePayload), instrumentId, "REAGENT_RESPONSE", eventId);
                    log.warn("[Kafka] Install failed - insufficient quantity for reagent: {}", r.getName());
                }
            } else {
                responsePayload.put("status", "FAIL");
                responsePayload.put("reason", "Reagent not found in warehouse");
                createOutboxEvent("REAGENT_INSTALL_RESPONSE", List.of(responsePayload), instrumentId, "REAGENT_RESPONSE", eventId);
                log.warn("[Kafka] Install failed - reagent not found: {}", reagentIdStr);
            }
            saveInboxEvent(eventId, event);
        } catch (Exception e) {
            log.error("Failed to process install request: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.reagent.uninstall.request:warehouse.reagent.uninstall.request}", groupId = "warehouse-service-reagent", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onUninstallRequest(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> event = record.value();
            UUID eventId = UUID.fromString((String) event.get("eventId"));

            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Reagent uninstall request {} already processed, skipping.", eventId);
                return;
            }

            log.info("[Kafka] Received uninstall request: {}", event);

            List<Map<String, Object>> payload = (List<Map<String, Object>>) event.get("payload");
            if (payload == null || payload.isEmpty()) {
                log.warn("Payload is empty for uninstall request");
                return;
            }

            Map<String, Object> requestPayload = payload.get(0);
            String instrumentId = (String) requestPayload.get("instrumentId");
            String reagentIdStr = (String) requestPayload.get("reagentId");
            UUID reagentId = UUID.fromString(reagentIdStr);
            String removedBy = (String) requestPayload.get("removedBy");
            Number quantityRemainingNumber = (Number) requestPayload.get("quantityRemaining");
            BigDecimal quantityToReturn = (quantityRemainingNumber != null) ? new BigDecimal(quantityRemainingNumber.toString()) : BigDecimal.ZERO;

            if (quantityToReturn.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("[Kafka] Uninstall failed - invalid quantity to return for reagent: {}", reagentIdStr);
                saveInboxEvent(eventId, event);
                return;
            }

            try {
                UUID.fromString(instrumentId);
            } catch (Exception ex) {
                log.warn("[Kafka] Uninstall failed - invalid instrumentId: {}", instrumentId);
                saveInboxEvent(eventId, event);
                return;
            }

            if (instrumentRepository.findById(UUID.fromString(instrumentId)).isEmpty()) {
                log.warn("[Kafka] Uninstall failed - instrument not found: {}", instrumentId);
                saveInboxEvent(eventId, event);
                return;
            }

            Optional<Reagent> found = reagentsRepository.findById(reagentId);
            if (found.isPresent()) {
                Reagent r = found.get();

                // Determine if reagent supply is expired
                ReagentSupplyHistory supply = supplyHistoryRepository.findReagentSupplyHistoriesByReagent(r);
                LocalDate expirationDate = (supply != null) ? supply.getExpirationDate() : null;

                ReagentUsageHistory usage = new ReagentUsageHistory();
                usage.setReagent(r);
                usage.setUsageDate(LocalDateTime.now());
                usage.setUsedBy(removedBy);

                if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
                    // Reagent is expired -> record uninstall as expired, no quantity added back
                    usage.setQuantityUsed(BigDecimal.ZERO);
                    usage.setAction("UNINSTALL_EXPIRED");
                    usage.setNote("Uninstalled expired reagent. ExpirationDate=" + expirationDate + "");
                    log.info("[Kafka] Uninstall recorded as expired for reagent={} instrument={} expiration={}", r.getName(), instrumentId, expirationDate);
                } else {
                    // Normal uninstall - return quantity to warehouse (negative value in usage history)
                    usage.setQuantityUsed(quantityToReturn.negate()); // Negative value for return
                    usage.setAction("UNINSTALL");
                    usage.setNote("Uninstalled from instrument " + instrumentId);
                    log.info("[Kafka] Uninstall success for reagent={} instrument={}", r.getName(), instrumentId);
                }

                usageHistoryRepository.save(usage);
            } else {
                log.warn("[Kafka] Uninstall failed - reagent not found: {}", reagentIdStr);
            }
            saveInboxEvent(eventId, event);
        } catch (Exception e) {
            log.error("Failed to process uninstall request: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.reagent.sync.request:warehouse.reagent.sync.request}", groupId = "warehouse-service-reagent", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onSyncRequest(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> event = record.value();
            UUID eventId = UUID.fromString((String) event.get("eventId"));

            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Reagent sync request {} already processed, skipping.", eventId);
                return;
            }

            log.info("[Kafka] Received sync request: {}", event);

            List<Map<String, Object>> payload = (List<Map<String, Object>>) event.get("payload");
            if (payload == null || payload.isEmpty()) {
                log.warn("Payload is empty for sync request");
                return;
            }

            Map<String, Object> requestPayload = payload.get(0);
            String instrumentId = (String) requestPayload.get("instrumentId");
            String installedBy = (String) requestPayload.get("installedBy");

            Map<String, Object> responsePayload = new HashMap<>();
            responsePayload.put("instrumentId", instrumentId);

            UUID instrumentUuid;
            try {
                instrumentUuid = UUID.fromString(instrumentId);
            } catch (Exception ex) {
                responsePayload.put("status", "FAIL");
                responsePayload.put("reason", "Invalid instrumentId");
                createOutboxEvent("REAGENT_SYNC_RESPONSE", List.of(responsePayload), instrumentId, "REAGENT_RESPONSE", eventId);
                log.warn("[Kafka] Sync failed - invalid instrumentId: {}", instrumentId);
                saveInboxEvent(eventId, event);
                return;
            }

            Optional<Instrument> instrumentOpt = instrumentRepository.findById(instrumentUuid);
            if (instrumentOpt.isEmpty()) {
                responsePayload.put("status", "FAIL");
                responsePayload.put("reason", "Instrument not found");
                createOutboxEvent("REAGENT_SYNC_RESPONSE", List.of(responsePayload), instrumentId, "REAGENT_RESPONSE", eventId);
                log.warn("[Kafka] Sync failed - instrument not found: {}", instrumentId);
                saveInboxEvent(eventId, event);
                return;
            }

            Instrument instrument = instrumentOpt.get();
            List<InstrumentReagents> defaultReagents = instrumentReagentsRepository.findAllByInstrument(instrument);

            if (defaultReagents.isEmpty()) {
                createOutboxEvent("REAGENT_SYNC_RESPONSE", List.of(), instrumentId, "REAGENT_RESPONSE", eventId);
                log.info("[Kafka] Sync completed - no default reagents for instrument: {}", instrumentId);
                saveInboxEvent(eventId, event);
                return;
            }

            // New: validate expiration for all default reagents before syncing
            for (InstrumentReagents defaultReagent : defaultReagents) {
                ReagentSupplyHistory supply = supplyHistoryRepository.findReagentSupplyHistoriesByReagent(defaultReagent.getReagent());
                LocalDate exp = (supply != null) ? supply.getExpirationDate() : null;
                if (exp != null && exp.isBefore(LocalDate.now())) {
                    Map<String, Object> failPayload = new HashMap<>();
                    failPayload.put("status", "FAIL");
                    failPayload.put("reason", "Default reagent '" + defaultReagent.getReagent().getName() + "' expired on " + exp);
                    failPayload.put("reagentId", defaultReagent.getReagent().getReagentId().toString());
                    createOutboxEvent("REAGENT_SYNC_RESPONSE", List.of(failPayload), instrumentId, "REAGENT_RESPONSE", eventId);
                    log.warn("[Kafka] Sync aborted - default reagent expired: {} (expired {}) for instrument {}", defaultReagent.getReagent().getName(), exp, instrumentId);
                    saveInboxEvent(eventId, event);
                    return;
                }
            }

            for (InstrumentReagents defaultReagent : defaultReagents) {
                ReagentUsageHistory usage = new ReagentUsageHistory();
                usage.setReagent(defaultReagent.getReagent());
                usage.setQuantityUsed(BigDecimal.valueOf(defaultReagent.getQuantity()));
                usage.setUsageDate(LocalDateTime.now());
                usage.setUsedBy(installedBy);
                usage.setAction("SYNC-INSTALL");
                usage.setNote("Default reagent installed on instrument " + instrumentId);
                usageHistoryRepository.save(usage);
            }

            List<Map<String, Object>> items = defaultReagents.stream().map(item -> {
                Map<String, Object> info = new HashMap<>();
                ReagentSupplyHistory supply = supplyHistoryRepository.findReagentSupplyHistoriesByReagent(item.getReagent());
                info.put("instrumentId", instrumentId);
                info.put("installedAt", LocalDateTime.now());
                info.put("lotNumber", supply != null ? supply.getLotNumber() : "N/A");
                info.put("vendorName", "demo");
                info.put("lastCheckedAt", LocalDateTime.now());
                info.put("unit", supply != null ? supply.getUnitOfMeasure() : "N/A");
                info.put("uninstalledAt", null);
                info.put("reagentName", item.getReagent().getName().toUpperCase());
                info.put("quantityRemaining", item.getQuantity());
                info.put("inUse", true);
                info.put("removedBy", null);
                info.put("installedBy", installedBy);
                info.put("reagentId", item.getReagent().getReagentId().toString());
                info.put("expirationDate", supply != null ? supply.getExpirationDate() : null);
                info.put("status", "INSTALLED");
                return info;
            }).collect(Collectors.toList());

            createOutboxEvent("REAGENT_SYNC_RESPONSE", items, instrumentId, "REAGENT_RESPONSE", eventId);
            log.info("[Kafka] Sent sync as install-style response with {} reagents for instrument={}", items.size(), instrumentId);
            saveInboxEvent(eventId, event);

        } catch (Exception e) {
            log.error("Failed to process sync request: {}", e.getMessage(), e);
        }
    }

    private void createOutboxEvent(String eventType, List<Map<String, Object>> payload, String aggregateId, String aggregateType, UUID originalEventId) throws Exception {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", originalEventId.toString());
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

    private void saveInboxEvent(UUID eventId, Map<String, Object> event) throws Exception {
        InboxEvent inboxEvent = new InboxEvent();
        inboxEvent.setEventId(eventId);
        inboxEvent.setPayload(objectMapper.writeValueAsString(event));
        inboxEvent.setProcessedAt(Instant.now());
        inboxRepository.save(inboxEvent);
        log.info("Marked event {} as processed.", eventId);
    }
}
