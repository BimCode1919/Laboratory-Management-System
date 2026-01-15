package org.overcode250204.instrumentservice.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmihalcea.hibernate.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.dtos.ChangeModeCommand;
import org.overcode250204.instrumentservice.dtos.ConfigurationDTO;
import org.overcode250204.instrumentservice.dtos.InstrumentDTO;
import org.overcode250204.instrumentservice.dtos.InstrumentSyncDTO;
import org.overcode250204.instrumentservice.dtos.ModeChangeResultDTO;
import org.overcode250204.instrumentservice.entity.Instrument;
import org.overcode250204.instrumentservice.entity.InstrumentConfiguration;
import org.overcode250204.instrumentservice.entity.InstrumentEventLog;
import org.overcode250204.instrumentservice.entity.InboxEvent;
import org.overcode250204.instrumentservice.enums.InstrumentMode;
import org.overcode250204.instrumentservice.enums.InstrumentRunStatus;
import org.overcode250204.instrumentservice.events.InstrumentEventPublisher;
import org.overcode250204.instrumentservice.events.SystemEventPublisher;
import org.overcode250204.instrumentservice.exception.ErrorCode;
import org.overcode250204.instrumentservice.exception.InstrumentException;
import org.overcode250204.instrumentservice.grpc.InstrumentWarehouseClientService;
import org.overcode250204.instrumentservice.repository.InstrumentConfigurationRepository;
import org.overcode250204.instrumentservice.repository.InstrumentEventLogRepository;
import org.overcode250204.instrumentservice.repository.InstrumentRepository;
import org.overcode250204.instrumentservice.repository.InstrumentRunRepository;
import org.overcode250204.instrumentservice.repository.InboxRepository;
import org.overcode250204.instrumentservice.service.interfaces.InstrumentService;
import org.overcode250204.instrumentservice.utils.AuthUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentRunRepository runRepository;
    private final InstrumentEventLogRepository eventLogRepository;
    private final SystemEventPublisher eventPublisher;
    private final InstrumentEventPublisher instrumentEventPublisher;
    private final InboxRepository inboxRepository;
    private final ObjectMapper objectMapper;
    private final InstrumentConfigurationRepository instrumentConfigurationRepository;
    private final InstrumentWarehouseClientService instrumentWarehouseClientService;


    @Override
    public ModeChangeResultDTO changeMode(UUID instrumentId, ChangeModeCommand req) {

        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new InstrumentException(ErrorCode.INSTRUMENT_NOT_FOUND));

        InstrumentMode oldMode = instrument.getMode();
        InstrumentMode newMode = req.getMode();

        validateModeChange(instrumentId, oldMode, newMode, req);


        instrument.setMode(newMode);
        instrument.setLastModeChangeAt(LocalDateTime.now());
        instrument.setUpdatedAt(LocalDateTime.now());
        instrumentRepository.save(instrument);


        eventPublisher.publishMonitoringEvent(
                "INSTRUMENT_MODE_CHANGED",
                Map.of(
                        "instrumentId", instrument.getId().toString(),
                        "oldMode", oldMode.name(),
                        "newMode", newMode.name(),
                        "performedBy", req.getUserId().toString(),
                        "reason", req.getReason(),
                        "changedAt", LocalDateTime.now().toString()
                )
        );

        saveAuditLog(instrument, oldMode, newMode, req);

        return ModeChangeResultDTO.builder()
                .instrumentId(instrument.getId())
                .oldMode(oldMode)
                .newMode(newMode)
                .reason(req.getReason())
                .performedBy(req.getUserId())
                .changedAt(instrument.getLastModeChangeAt())
                .build();
    }

    @Override
    public List<InstrumentDTO> getAllInstruments() {
        return instrumentRepository.findAll().stream()
                .map(this::toInstrumentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InstrumentDTO getInstrumentById(UUID id) {
        return instrumentRepository.findById(id)
                .map(this::toInstrumentDTO)
                .orElseThrow(() -> new InstrumentException(ErrorCode.INSTRUMENT_NOT_FOUND));
    }

    private InstrumentDTO toInstrumentDTO(Instrument instrument) {
        List<ConfigurationDTO> configs = instrument.getConfigurations() == null
                ? Collections.emptyList()
                : instrument.getConfigurations().stream()
                .map(this::toConfigurationDTO)
                .collect(Collectors.toList());

        return new InstrumentDTO(
                instrument.getId(),
                instrument.getInstrumentCode(),
                instrument.getName(),
                instrument.getModel(),
                instrument.getSerialNumber(),
                instrument.getLocation(),
                instrument.getStatus(),
                instrument.getMode(),
                instrument.getIsOnline(),
                instrument.getConfigVersion(),
                instrument.getLastConfigSyncAt(),
                configs
        );
    }

    private ConfigurationDTO toConfigurationDTO(InstrumentConfiguration cfg) {
        if (cfg == null) return null;
        ConfigurationDTO dto = new ConfigurationDTO();
        dto.setConfigId(cfg.getConfigId());
        dto.setConfigName(cfg.getConfigName());
        dto.setConfigKey(cfg.getConfigKey());
        dto.setConfigValue(cfg.getConfigValue());
        dto.setDefaultValue(cfg.getDefaultValue());
        dto.setDescription(cfg.getDescription());
        dto.setIsGlobal(cfg.getIsGlobal());
        dto.setCreatedAt(cfg.getCreatedAt());
        dto.setUpdatedAt(cfg.getUpdatedAt());
        dto.setCreatedBy(cfg.getCreatedBy());
        dto.setUpdatedBy(cfg.getUpdatedBy());
        return dto;
    }


    private void validateModeChange(UUID instrumentId, InstrumentMode oldMode,
                                    InstrumentMode newMode, ChangeModeCommand req) {
        if (newMode == null)
            throw new InstrumentException(ErrorCode.INVALID_REQUEST);
        if (oldMode == newMode)
            throw new InstrumentException(ErrorCode.INVALID_MODE_TRANSITION);
        if (!isValidModeTransition(oldMode, newMode))
            throw new InstrumentException(ErrorCode.INVALID_MODE_TRANSITION);
        if (runRepository.existsByInstrumentIdAndStatus(instrumentId, InstrumentRunStatus.RUNNING))
            throw new InstrumentException(ErrorCode.INSTRUMENT_BUSY);
        if (requiresReason(newMode) && StringUtils.isBlank(req.getReason()))
            throw new InstrumentException(ErrorCode.INVALID_REQUEST);
    }

    private boolean isValidModeTransition(InstrumentMode oldMode, InstrumentMode newMode) {
        return switch (oldMode) {
            case READY -> (newMode == InstrumentMode.MAINTENANCE || newMode == InstrumentMode.INACTIVE);
            case MAINTENANCE, INACTIVE -> (newMode == InstrumentMode.READY);
            default -> false;
        };
    }

    private boolean requiresReason(InstrumentMode mode) {
        return mode == InstrumentMode.MAINTENANCE || mode == InstrumentMode.INACTIVE;
    }

    private void saveAuditLog(Instrument instrument, InstrumentMode oldMode,
                              InstrumentMode newMode, ChangeModeCommand req) {
        InstrumentEventLog logDoc = new InstrumentEventLog();
        logDoc.setInstrumentId(instrument.getId());
        logDoc.setEventType("MODE_CHANGE");
        logDoc.setPerformedBy(req.getUserId());
        logDoc.setTimestamp(LocalDateTime.now());

        Map<String, Object> details = Map.of(
                "oldMode", oldMode.name(),
                "newMode", newMode.name(),
                "reason", req.getReason()
        );

        ObjectMapper mapper = new ObjectMapper();
        logDoc.setDetails(mapper.valueToTree(details));

        eventLogRepository.save(logDoc);
    }

    @Override
    @Transactional
    public void syncInstrument(InstrumentSyncDTO instrumentSyncDTO) {
        log.debug("Syncing instrument with ID: {}", instrumentSyncDTO.getId());
        Instrument instrument = instrumentRepository.findById(instrumentSyncDTO.getId())
                .orElse(new Instrument());

        if (instrumentSyncDTO.getConfigurations() != null) {
            log.debug("Incoming configurations count={} for instrumentId={}", instrumentSyncDTO.getConfigurations().size(), instrumentSyncDTO.getId());
        } else {
            log.debug("No incoming configurations for instrumentId={}", instrumentSyncDTO.getId());
        }

        updateInstrumentFromSyncDTO(instrument, instrumentSyncDTO);

        if (instrumentSyncDTO.getConfigurations() != null && !instrumentSyncDTO.getConfigurations().isEmpty()) {
            syncConfigurations(instrument, instrumentSyncDTO.getConfigurations());
        }

        instrumentRepository.save(instrument);

        try {
            Optional<Instrument> persisted = instrumentRepository.findById(instrument.getId());
            int persistedCount = persisted.map(i -> i.getConfigurations() == null ? 0 : i.getConfigurations().size()).orElse(0);
            log.info("Successfully synced instrument with ID: {}. Persisted configurations count={}", instrument.getId(), persistedCount);
        } catch (Exception ex) {
            log.info("Successfully synced instrument with ID: {} (failed to fetch persisted configurations: {})", instrument.getId(), ex.getMessage());
        }
    }

    private void updateInstrumentFromSyncDTO(Instrument instrument, InstrumentSyncDTO dto) {
        if (instrument.getId() == null) {
            instrument.setId(dto.getId());
        }
        instrument.setName(dto.getName());
        instrument.setModel(dto.getModel());
        instrument.setSerialNumber(dto.getSerialNumber());
        instrument.setStatus(dto.getStatus());
        instrument.setCreatedAt(dto.getCreatedAt());
        instrument.setCreatedBy(dto.getCreatedBy());
        instrument.setUpdatedAt(dto.getUpdatedAt());
        instrument.setLastModeChangeAt(dto.getLastModeChangeAt());
        instrument.setLastConfigSyncAt(dto.getLastConfigSyncAt());
        instrument.setConfigVersion(dto.getConfigVersion());
        instrument.setInstrumentCode(dto.getInstrumentCode());
        instrument.setLocation(dto.getLocation());
        instrument.setMode(dto.getMode());
        instrument.setIsOnline(dto.getIsOnline());
        instrument.setLastModeChangeAt(LocalDateTime.now());
        instrument.setLastConfigSyncAt(LocalDateTime.now());

        if (dto.getConfigurations() != null) {

        }
    }

    private void syncConfigurations(Instrument instrument, List<ConfigurationDTO> incoming) {
        if (incoming == null) return;

        List<InstrumentConfiguration> managed = instrument.getConfigurations();
        if (managed == null) {
            managed = new ArrayList<>();
            instrument.setConfigurations(managed);
        }

        log.debug("Existing configurations count={} for instrumentId={}", managed.size(), instrument.getId());

        Map<UUID, InstrumentConfiguration> existingById = new HashMap<>();
        for (InstrumentConfiguration cfg : new ArrayList<>(managed)) {
            if (cfg.getConfigId() != null) existingById.put(cfg.getConfigId(), cfg);
        }

        List<InstrumentConfiguration> toKeep = new ArrayList<>();

        for (ConfigurationDTO dto : incoming) {
            UUID cfgId = dto.getConfigId() != null ? dto.getConfigId() : UUID.randomUUID();

            InstrumentConfiguration cfg = existingById.get(cfgId);

            if (cfg == null) {
                cfg = InstrumentConfiguration.builder().configId(cfgId).build();
                cfg.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
            }

            cfg.setConfigName(dto.getConfigName());
            cfg.setConfigKey(dto.getConfigKey());
            cfg.setConfigValue(dto.getConfigValue());
            cfg.setDefaultValue(dto.getDefaultValue());
            cfg.setDescription(dto.getDescription());
            cfg.setIsGlobal(dto.getIsGlobal() != null ? dto.getIsGlobal() : false);
            if (dto.getUpdatedAt() != null) cfg.setUpdatedAt(dto.getUpdatedAt());
            if (cfg.getCreatedAt() == null) cfg.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
            cfg.setCreatedBy(dto.getCreatedBy());
            cfg.setUpdatedBy(dto.getUpdatedBy());

            cfg.setInstrument(instrument);

            log.debug("Prepared configuration for save: configId={}, key={}, value={}", cfg.getConfigId(), cfg.getConfigKey(), cfg.getConfigValue());

            toKeep.add(cfg);
        }

        managed.removeIf(existing -> toKeep.stream().noneMatch(k -> k.getConfigId().equals(existing.getConfigId())));

        for (InstrumentConfiguration cfg : toKeep) {
            boolean found = false;
            for (int i = 0; i < managed.size(); i++) {
                InstrumentConfiguration m = managed.get(i);
                if (m.getConfigId().equals(cfg.getConfigId())) {
                    // update existing managed entity fields
                    m.setConfigName(cfg.getConfigName());
                    m.setConfigKey(cfg.getConfigKey());
                    m.setConfigValue(cfg.getConfigValue());
                    m.setDefaultValue(cfg.getDefaultValue());
                    m.setDescription(cfg.getDescription());
                    m.setIsGlobal(cfg.getIsGlobal());
                    m.setUpdatedAt(cfg.getUpdatedAt());
                    m.setCreatedBy(cfg.getCreatedBy());
                    m.setUpdatedBy(cfg.getUpdatedBy());
                    m.setInstrument(instrument);
                    found = true;
                    break;
                }
            }
            if (!found) {
                managed.add(cfg);
            }
        }

        log.debug("Updated managed configurations on instrumentId={} now has {} items", instrument.getId(), managed.size());
    }

    @Override
    public JsonNode requestConfigurationSync(UUID instrumentId) {

        try {
            var instrumentInfo = instrumentWarehouseClientService.getInstrument(instrumentId);
            if (instrumentInfo == null) {
                log.debug("Instrument {} not found in warehouse when requesting configuration sync", instrumentId);
                throw new InstrumentException(ErrorCode.INSTRUMENT_NOT_FOUND);
            }
        } catch (InstrumentException ie) {
            throw ie;
        } catch (Exception ex) {
            log.error("Failed to validate instrument {} existence via warehouse gRPC: {}", instrumentId, ex.getMessage());
            throw new InstrumentException(ErrorCode.INSTRUMENT_CONFIGURATION_SYNC_FAILED);
        }

        String eventId = instrumentEventPublisher.publishConfigurationSyncRequest(instrumentId.toString());

        if (eventId == null) {
            log.error("Failed to create outbox event for configuration sync");
            throw new InstrumentException(ErrorCode.INSTRUMENT_CONFIGURATION_SYNC_FAILED);
        }

        // publish monitoring log event for config sync request
        UUID userId = null;
        try {
            userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        } catch (Exception ignored) {}

        Map<String, Object> monitoringPayload = (userId != null)
                ? Map.of("instrumentId", instrumentId.toString(), "eventId", eventId, "performedBy", userId.toString())
                : Map.of("instrumentId", instrumentId.toString(), "eventId", eventId);
        eventPublisher.publishMonitoringEvent("INSTRUMENT_CONFIGURATION_SYNC_REQUEST_LOG", monitoringPayload);

        final var eventUuid = UUID.fromString(eventId);
        final long timeoutMs = 5000L;
        final long pollIntervalMs = 200L;
        long waited = 0L;

        try {
            while (waited < timeoutMs) {
                if (inboxRepository.existsByEventId(eventUuid)) {
                    String payload = inboxRepository.findByEventId(eventUuid).map(InboxEvent::getPayload).orElse(null);
                    if (payload == null) return null;
                    try {
                        return objectMapper.readTree(payload);
                    } catch (Exception ex) {
                        log.warn("Failed to parse inbox payload for event {}", eventId, ex);
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

    @Scheduled(cron = "0 0 2 * * ?")
    @Override
    public JsonNode requestConfigurationAllSync() {
        String eventId = instrumentEventPublisher.publishConfigurationAllSyncRequest();

        if (eventId == null) {
            log.error("Failed to create outbox event for CONFIGURATION_ALL_SYNC_REQUEST");
            throw new InstrumentException(ErrorCode.INSTRUMENT_CONFIGURATION_ALL_SYNC_FAILED);
        }

        // publish monitoring log event for all-config sync request
        UUID userId = null;
        try {
            userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        } catch (Exception ignored) {}

        Map<String, Object> monitoringAllPayload = (userId != null)
                ? Map.of("eventId", eventId, "performedBy", userId.toString())
                : Map.of("eventId", eventId);
        eventPublisher.publishMonitoringEvent("INSTRUMENT_CONFIGURATION_ALL_SYNC_REQUEST_LOG", monitoringAllPayload);

        final var eventUuid = UUID.fromString(eventId);
        final long timeoutMs = 5000L;
        final long pollIntervalMs = 200L;
        long waited = 0L;

        try {
            while (waited < timeoutMs) {
                if (inboxRepository.existsByEventId(eventUuid)) {
                    String payload = inboxRepository.findByEventId(eventUuid).map(InboxEvent::getPayload).orElse(null);
                    if (payload == null) return null;
                    try {
                        return objectMapper.readTree(payload);
                    } catch (Exception ex) {
                        log.warn("Failed to parse inbox payload for event {}", eventId, ex);
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

}
