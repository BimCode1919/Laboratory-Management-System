package org.overcode250204.instrumentservice.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.configs.ReagentConsumptionConfig;
import org.overcode250204.instrumentservice.dtos.InstallReagentCommand;
import org.overcode250204.instrumentservice.dtos.ReagentDTO;
import org.overcode250204.instrumentservice.dtos.UninstallReagentCommand;
import org.overcode250204.instrumentservice.entity.InboxEvent;
import org.overcode250204.instrumentservice.entity.InstalledReagent;
import org.overcode250204.instrumentservice.events.ReagentEventPublisher;
import org.overcode250204.instrumentservice.events.SystemEventPublisher;
import org.overcode250204.instrumentservice.exception.ErrorCode;
import org.overcode250204.instrumentservice.exception.InstrumentException;
import org.overcode250204.instrumentservice.grpc.InstrumentWarehouseClientService;
import org.overcode250204.instrumentservice.repository.InboxRepository;
import org.overcode250204.instrumentservice.repository.InstalledReagentRepository;
import org.overcode250204.instrumentservice.repository.InstrumentRepository;
import org.overcode250204.instrumentservice.service.interfaces.ReagentService;
import org.overcode250204.instrumentservice.utils.AuthUtils;
import org.overcode250204.common.grpc.ReagentInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReagentServiceImpl implements ReagentService {

    private final ReagentConsumptionConfig reagentConfig;
    private final InstalledReagentRepository reagentRepository;
    private final InstrumentRepository instrumentRepository;
    private final InboxRepository inboxRepository;
    private final ObjectMapper objectMapper;
    private final ReagentEventPublisher reagentEventPublisher;
    private final SystemEventPublisher systemEventPublisher;
    private final InstrumentWarehouseClientService warehouseClientService;

    // Kiểm tra lượng reagent đủ cho quá trình chạy test
    @Override
    public boolean hasSufficientReagent(UUID instrumentId, String testType, int samplesNeeded) {

        Map<String, Double> reagentUsage = reagentConfig.getConsumption().get(testType.toUpperCase());
        if (reagentUsage == null || reagentUsage.isEmpty()) {
            log.warn("⚠ No reagent definition found for testType={}", testType);
            return false;
        }

        for (Map.Entry<String, Double> entry : reagentUsage.entrySet()) {
            String reagentType = entry.getKey();
            double required = entry.getValue() * samplesNeeded;
            List<InstalledReagent> reagents = reagentRepository
                    .findByInstrumentIdAndReagentNameAndInUseTrueOrderByInstalledAtAsc(instrumentId, reagentType);

            if (reagents.isEmpty()) {
                log.warn("⚠ No reagent in use for type={} instrument={}", reagentType, instrumentId);
                return false;
            }
            double totalAvailable = 0.0;

            for (InstalledReagent r : reagents) {
                double remain = r.getQuantityRemaining() != null ? r.getQuantityRemaining() : 0.0;
                totalAvailable += remain;
            }

            if (totalAvailable < required) {
                log.warn("Not enough reagent {} for testType={} (needed={}, all candidates insufficient)", reagentType, testType, required);
                return false;
            }
        }

        log.info("Reagent check passed for instrument={} testType={} samples={}", instrumentId, testType, samplesNeeded);
        return true;
    }


    // Tiêu hao reagent khi chạy test
    @Override
    @Transactional
    public void consumeReagent(UUID instrumentId, String testType, int samplesUsed) {
        Map<String, Double> reagents = reagentConfig.getConsumption().get(testType.toUpperCase());
        if (reagents == null || reagents.isEmpty()) {
            log.warn("No reagent definition for testType={}", testType);
            return;
        }

        reagents.forEach((reagentType, perSample) -> {
            double total = perSample * samplesUsed;
            List<InstalledReagent> candidates = reagentRepository
                    .findByInstrumentIdAndReagentNameAndInUseTrueOrderByInstalledAtAsc(instrumentId, reagentType);

            if (candidates.isEmpty()) {
                throw new InstrumentException(ErrorCode.REAGENT_NOT_FOUND);
            }

            InstalledReagent selected = candidates.stream()
                    .filter(r -> Optional.ofNullable(r.getQuantityRemaining()).orElse(0.0) >= total)
                    .findFirst()
                    .orElseThrow(() -> new InstrumentException(ErrorCode.REAGENT_LOW));

            double remaining = Optional.ofNullable(selected.getQuantityRemaining()).orElse(0.0);
            selected.setQuantityRemaining(remaining - total);

            if (selected.getQuantityRemaining() <= 0) {
                selected.setInUse(false);
            }

            reagentRepository.save(selected);
            log.info("Consumed {} mL of {} for {}", total, reagentType, testType);

        });
    }

    @Override
    public List<ReagentDTO> getInstalledReagents(UUID instrumentId) {
        return reagentRepository.findByInstrumentId(instrumentId).stream()
                .map(ReagentDTO::new)
                .collect(Collectors.toList());
    }

    // Chụp snapshot trạng thái reagent hiện tại
    @Override
    public JsonNode snapshotReagent(UUID instrumentId) {
        List<InstalledReagent> reagents = reagentRepository.findByInstrumentIdAndInUseTrue(instrumentId);
        ObjectNode root = objectMapper.createObjectNode();
        root.put("count", reagents.size());

        for (InstalledReagent r : reagents) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("name", r.getReagentName());
            node.put("lot", r.getLotNumber());
            node.put("remaining", r.getQuantityRemaining());
            node.put("unit", r.getUnit());
            root.set(r.getReagentId().toString(), node);
        }
        return root;
    }

    @Override
    public JsonNode requestInstallReagent(UUID instrumentId, InstallReagentCommand cmd) {
        if (cmd.getQuantity() <= 0) {
            throw new InstrumentException(ErrorCode.INVALID_QUANTITY);
        }

        instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new InstrumentException(ErrorCode.INSTRUMENT_NOT_FOUND));

        Map<String, Object> payload = new HashMap<>();
        payload.put("instrumentId", instrumentId.toString());
        payload.put("reagentId", cmd.getReagentId() != null ? cmd.getReagentId().toString() : null);
        payload.put("quantity", cmd.getQuantity());
        UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        payload.put("installedBy", userId.toString());

        // Validate against warehouse via gRPC if reagentId provided
        if (cmd.getReagentId() != null) {
            try {
                ReagentInfo ri = warehouseClientService.getReagent(cmd.getReagentId());
                if (ri == null || ri.getReagentId().isEmpty()) {
                    log.error("Warehouse returned no data for reagent {}", cmd.getReagentId());
                    throw new InstrumentException(ErrorCode.REAGENT_NOT_FOUND);
                }

                // parse available quantity (ri.quantity) and totalQuantity
                BigDecimal available = BigDecimal.ZERO;
                BigDecimal totalQty = BigDecimal.ZERO;
                try {
                    String q = ri.getQuantity();
                    if (q != null && !q.isEmpty()) available = new BigDecimal(q);
                } catch (Exception e) {
                    log.warn("Failed to parse available quantity '{}' for reagent {}", ri.getQuantity(), cmd.getReagentId());
                }
                try {
                    String tq = ri.getTotalQuantity();
                    if (tq != null && !tq.isEmpty()) totalQty = new BigDecimal(tq);
                } catch (Exception e) {
                    log.warn("Failed to parse totalQuantity '{}' for reagent {}", ri.getTotalQuantity(), cmd.getReagentId());
                }

                // parse expirationDate
                LocalDate expiration = null;
                try {
                    String exp = ri.getExpirationDate();
                    if (exp != null && !exp.isEmpty()) expiration = LocalDate.parse(exp);
                } catch (Exception e) {
                    log.warn("Failed to parse expirationDate '{}' for reagent {}", ri.getExpirationDate(), cmd.getReagentId());
                }

                // Check expiration
                if (expiration != null && expiration.isBefore(LocalDate.now())) {
                    log.warn("Reagent {} is expired at {}", cmd.getReagentId(), expiration);
                    throw new InstrumentException(ErrorCode.REAGENT_EXPIRED);
                }

                // Check quantities: available should be >= requested quantity
                BigDecimal requested = BigDecimal.valueOf(cmd.getQuantity());
                if (available.compareTo(requested) < 0) {
                    log.warn("Insufficient available quantity for reagent {}: available={}, requested={}", cmd.getReagentId(), available, requested);
                    throw new InstrumentException(ErrorCode.REAGENT_LOW);
                }

                // Optionally check totalQty as well - if total < requested, treat as warehouse validation failed
                if (totalQty.compareTo(requested) < 0) {
                    log.warn("Warehouse total quantity insufficient for reagent {}: total={}, requested={}", cmd.getReagentId(), totalQty, requested);
                    throw new InstrumentException(ErrorCode.WAREHOUSE_VALIDATION_FAILED);
                }

            } catch (InstrumentException ie) {
                throw ie; // rethrow known exceptions
            } catch (Exception ex) {
                log.error("Failed to validate reagent {} against warehouse: {}", cmd.getReagentId(), ex.getMessage(), ex);
                throw new InstrumentException(ErrorCode.WAREHOUSE_VALIDATION_FAILED);
            }
        }

        String eventId = reagentEventPublisher.publishInstallRequest(instrumentId.toString(), payload);

        // Publish monitoring event for logging
        if (eventId != null) {
            systemEventPublisher.publishMonitoringEvent("REAGENT_INSTALL_REQUEST_LOG", Map.of(
                    "instrumentId", instrumentId.toString(),
                    "reagentId", cmd.getReagentId() != null ? cmd.getReagentId().toString() : null,
                    "quantity", cmd.getQuantity(),
                    "eventId", eventId,
                    "performedBy", userId.toString()
            ));
        }

        // Wait for inbox event to be processed by ReagentEventListener with a timeout
        if (eventId == null) {
            log.error("Failed to create outbox event for install request");
            throw new InstrumentException(ErrorCode.REAGENT_INSTALL_PUBLISH_FAILED);
        }

        final UUID eventUuid = UUID.fromString(eventId);
        final long timeoutMs = 5000L; // total wait time
        final long pollIntervalMs = 200L;
        long waited = 0L;

        try {
            while (waited < timeoutMs) {
                if (inboxRepository.existsByEventId(eventUuid)) {
                    String payloadStr = inboxRepository.findByEventId(eventUuid).map(InboxEvent::getPayload).orElse(null);
                    if (payloadStr == null) return null;
                    try {
                        return objectMapper.readTree(payloadStr);
                    } catch (Exception ex) {
                        log.warn("Failed to parse inbox payload json for event {}", eventId, ex);
                        return null;
                    }
                }
                Thread.sleep(pollIntervalMs);
                waited += pollIntervalMs;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for inbox event {}", eventId);
        }

        log.info("Timeout waiting for inbox event {} after {}ms", eventId, timeoutMs);
        return null;
    }
    // java
    @Override
    public JsonNode requestSyncReagent(UUID instrumentId) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("instrumentId", instrumentId.toString());
        UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        payload.put("installedBy", userId.toString());

        String eventId = reagentEventPublisher.publishSyncRequest(instrumentId.toString(), payload);

        if (eventId != null) {
            systemEventPublisher.publishMonitoringEvent("REAGENT_SYNC_REQUEST_LOG", Map.of(
                    "instrumentId", instrumentId.toString(),
                    "eventId", eventId,
                    "performedBy", userId.toString()
            ));
        }

        if (eventId == null) {
            log.error("Failed to create outbox event for sync request");
            throw new InstrumentException(ErrorCode.REAGENT_SYNC_PUBLISH_FAILED);
        }

        // Publish uninstall requests for each installed reagent and remove them locally
        List<InstalledReagent> installedReagents = reagentRepository.findByInstrumentId(instrumentId);
        for (InstalledReagent istalledReagent : installedReagents) {
            Map<String, Object> payloadSync = new HashMap<>();
            payloadSync.put("instrumentId", instrumentId.toString());
            payloadSync.put("reagentId", istalledReagent.getReagentId().toString());
            payloadSync.put("quantityRemaining", istalledReagent.getQuantityRemaining());
            payloadSync.put("removedBy", userId.toString());

            log.info("[Outbox] Uninstall payload: {}", payloadSync);
            reagentEventPublisher.publishUninstallRequest(instrumentId.toString(), payloadSync);
            // monitoring event per uninstall published
            systemEventPublisher.publishMonitoringEvent("REAGENT_UNINSTALL_REQUEST_LOG", Map.of(
                    "instrumentId", instrumentId.toString(),
                    "reagentId", istalledReagent.getReagentId().toString(),
                    "quantityRemaining", istalledReagent.getQuantityRemaining(),
                    "performedBy", userId.toString()
            ));
            reagentRepository.delete(istalledReagent);
        }

        final UUID eventUuid = UUID.fromString(eventId);
        final long timeoutMs = 5000L;
        final long pollIntervalMs = 200L;
        long waited = 0L;

        try {
            while (waited < timeoutMs) {
                if (inboxRepository.existsByEventId(eventUuid)) {
                    String payloadStr = inboxRepository.findByEventId(eventUuid).map(InboxEvent::getPayload).orElse(null);
                    if (payloadStr == null) return null;
                    try {
                        return objectMapper.readTree(payloadStr);
                    } catch (Exception ex) {
                        log.warn("Failed to parse inbox payload json for event {}", eventId, ex);
                        return null;
                    }
                }
                Thread.sleep(pollIntervalMs);
                waited += pollIntervalMs;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for inbox event {}", eventId);
        }

        log.info("Timeout waiting for inbox event {} after {}ms", eventId, timeoutMs);
        return null;
    }


    @Override
    public void requestUninstallReagent(UUID instrumentId, UninstallReagentCommand cmd) {
        instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new InstrumentException(ErrorCode.INSTRUMENT_NOT_FOUND));

        if (cmd.getReagentId() == null) {
            log.error("Uninstall request missing reagentId for instrument={}", instrumentId);
            throw new InstrumentException(ErrorCode.REAGENT_NOT_FOUND);
        }

        Double toRemove = Optional.ofNullable(cmd.getQuantityRemaining()).orElse(0.0);
        if (toRemove <= 0) {
            throw new InstrumentException(ErrorCode.INVALID_QUANTITY);
        }

        try {
            ReagentInfo ri = warehouseClientService.getReagent(cmd.getReagentId());
            if (ri == null || ri.getReagentId().isEmpty()) {
                log.error("Warehouse returned no data for reagent {}", cmd.getReagentId());
                throw new InstrumentException(ErrorCode.REAGENT_NOT_FOUND);
            }
        } catch (Exception ex) {
            log.error("Failed to validate reagent {} against warehouse: {}", cmd.getReagentId(), ex.getMessage(), ex);
            throw new InstrumentException(ErrorCode.WAREHOUSE_VALIDATION_FAILED);
        }

        InstalledReagent reagent = reagentRepository
                .findByInstrumentIdAndReagentId(instrumentId, cmd.getReagentId())
                .orElseThrow(() -> new InstrumentException(ErrorCode.REAGENT_NOT_INSTALLED));

        double currentQuantity = Optional.ofNullable(reagent.getQuantityRemaining()).orElse(0.0);
        if (toRemove > currentQuantity) {
            log.error("Uninstall quantity {} exceeds current quantity {} for reagent {} on instrument {}", toRemove, currentQuantity, reagent.getReagentName(), instrumentId);
            throw new InstrumentException(ErrorCode.REAGENT_UNINSTALL_INVALID_QUANTITY);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("instrumentId", instrumentId.toString());
        payload.put("reagentId", cmd.getReagentId().toString());
        payload.put("quantityRemaining", toRemove);
        UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        payload.put("removedBy", userId.toString());

        reagentEventPublisher.publishUninstallRequest(instrumentId.toString(), payload);

        // publish monitoring log for uninstall request
        try {
            systemEventPublisher.publishMonitoringEvent("REAGENT_UNINSTALL_REQUEST_LOG", Map.of(
                    "instrumentId", instrumentId.toString(),
                    "reagentId", cmd.getReagentId().toString(),
                    "quantityRemoved", toRemove,
                    "performedBy", userId.toString()
            ));
        } catch (Exception ex) {
            log.warn("Failed to publish monitoring event for reagent uninstall: {}", ex.getMessage());
        }

        double newQuantity = currentQuantity - toRemove;

        reagent.setQuantityRemaining(newQuantity);
        reagent.setInUse(newQuantity != 0.0);
        reagent.setStatus("UNINSTALLED");
        reagent.setUninstalledAt(LocalDateTime.now());
        reagent.setRemovedBy(userId);

        if (newQuantity <= 0.0) {
            reagentRepository.delete(reagent);
        } else {
            reagentRepository.save(reagent);
            log.info("Uninstalled reagent [{}] for instrument {}. Quantity set to {}. ", reagent.getReagentName(), reagent.getInstrument().getId(), newQuantity);
        }
    }

    @Override
    public ReagentDTO getReagent(UUID instrumentId, UUID reagentId) {
        InstalledReagent reagent = reagentRepository.findByInstrumentIdAndReagentId(instrumentId, reagentId)
                .orElseThrow(() -> new org.overcode250204.instrumentservice.exception.InstrumentException(org.overcode250204.instrumentservice.exception.ErrorCode.REAGENT_NOT_FOUND));
        return new ReagentDTO(reagent);
    }

    @Override
    public ReagentDTO updateReagentInUse(UUID instrumentId, UUID reagentId, org.overcode250204.instrumentservice.dtos.UpdateReagentInUseCommand cmd) {
        InstalledReagent reagent = reagentRepository.findByInstrumentIdAndReagentId(instrumentId, reagentId)
                .orElseThrow(() -> new org.overcode250204.instrumentservice.exception.InstrumentException(org.overcode250204.instrumentservice.exception.ErrorCode.REAGENT_NOT_FOUND));

        if (cmd.getInUse() == null) {
            throw new org.overcode250204.instrumentservice.exception.InstrumentException(org.overcode250204.instrumentservice.exception.ErrorCode.INVALID_REQUEST);
        }

        reagent.setInUse(cmd.getInUse());
        InstalledReagent saved = reagentRepository.save(reagent);
        return new ReagentDTO(saved);
    }

}
