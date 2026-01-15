package org.overcode250204.warehouseservice.services.implement;

import lombok.extern.slf4j.Slf4j;
import org.overcode250204.warehouseservice.events.publishers.MonitoringEventPublisher;
import org.overcode250204.warehouseservice.exceptions.ErrorCode;
import org.overcode250204.warehouseservice.exceptions.WarehouseException;
import org.overcode250204.warehouseservice.model.dto.instrument.CreateInstrumentRequest;
import org.overcode250204.warehouseservice.model.dto.instrumentReagent.CreateInstrumentReagentRequest;
import org.overcode250204.warehouseservice.model.entities.*;
import org.overcode250204.warehouseservice.model.enums.Status;
import org.overcode250204.warehouseservice.repositories.*;
import org.overcode250204.warehouseservice.services.interfaces.InstrumentService;
import org.overcode250204.warehouseservice.services.interfaces.ReagentInventoryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentsRepository instrumentsRepository;
    private final ConfigurationRepository configurationRepository;
    private final ReagentsRepository reagentsRepository;
    private final InstrumentReagentsRepository instrumentReagentsRepository;
    private final MonitoringEventPublisher eventPublisher;
    private final ReagentInventoryService reagentInventoryService;

    public InstrumentServiceImpl(
            InstrumentsRepository instrumentsRepository,
            ConfigurationRepository configurationRepository,
            ReagentsRepository reagentsRepository,
            InstrumentReagentsRepository instrumentReagentsRepository,
            MonitoringEventPublisher eventPublisher,
            ReagentInventoryService reagentInventoryService) {
        this.instrumentsRepository = instrumentsRepository;
        this.configurationRepository = configurationRepository;
        this.reagentsRepository = reagentsRepository;
        this.instrumentReagentsRepository = instrumentReagentsRepository;
        this.eventPublisher = eventPublisher;
        this.reagentInventoryService = reagentInventoryService;
    }

    @Override
    public Instrument createInstrument(CreateInstrumentRequest request, UUID createdBy) {

        if (request == null) {
            throw new WarehouseException(ErrorCode.INVALID_REQUEST);
        }

        boolean exists = instrumentsRepository.existsByNameOrSerialNumber(
                request.getName(), request.getSerialNumber());

        if (exists) {
            throw new WarehouseException(ErrorCode.INSTRUMENT_ALREADY_EXISTS);
        }

        // 1. Tạo object instrument nhưng chưa save
        Instrument instrument = Instrument.builder()
                .name(request.getName())
                .serialNumber(request.getSerialNumber())
                .model(request.getModel())
                .status(request.getStatus() != null ? request.getStatus() : Status.READY)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();

        // 2. Xử lý CONFIG
        List<Configuration> configsToUse = new ArrayList<>();

        // Clone từ instrument khác
        if (request.getCloneFromInstrumentId() != null) {
            Instrument source = instrumentsRepository.findById(request.getCloneFromInstrumentId())
                    .orElseThrow(() -> new WarehouseException(ErrorCode.INSTRUMENT_NOT_FOUND));

            List<Configuration> sourceConfigs = source.getConfigurations();

            configsToUse = sourceConfigs.stream()
                    .map(cfg -> Configuration.builder()
                            .isGlobal(false)
                            .configName(cfg.getConfigName())
                            .configKey(cfg.getConfigKey())
                            .configValue(cfg.getConfigValue())
                            .defaultValue(cfg.getDefaultValue())
                            .description(cfg.getDescription())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .createdBy(createdBy)
                            .instrument(instrument)
                            .build()
                    ).collect(Collectors.toList());
        }
        // Clone từ config IDs
        else if (request.getConfigurationIds() != null && !request.getConfigurationIds().isEmpty()) {

            List<Configuration> templates = configurationRepository.findAllById(request.getConfigurationIds());

            for (Configuration cfg : templates) {
                if (cfg.getInstrument() == null && !cfg.getIsGlobal()) {
                    cfg.setInstrument(instrument);
                    cfg.setUpdatedBy(createdBy);
                    cfg.setUpdatedAt(LocalDateTime.now());
                    configsToUse.add(cfg);
                } else {
                    Configuration clone = Configuration.builder()
                            .isGlobal(false)
                            .configName(cfg.getConfigName())
                            .configKey(cfg.getConfigKey())
                            .configValue(cfg.getDefaultValue())
                            .defaultValue(cfg.getDefaultValue())
                            .description(cfg.getDescription())
                            .createdAt(LocalDateTime.now())
                            .createdBy(createdBy)
                            .instrument(instrument)
                            .build();
                    configsToUse.add(clone);
                }
            }
        }
        // Lấy config global khi không có config nào khác
        else {
            List<Configuration> defaultConfigs = configurationRepository.findAll().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsGlobal()))
                    .map(cfg -> Configuration.builder()
                            .isGlobal(false)
                            .configName(cfg.getConfigName())
                            .configKey(cfg.getConfigKey())
                            .configValue(cfg.getDefaultValue())
                            .defaultValue(cfg.getDefaultValue())
                            .description(cfg.getDescription())
                            .createdAt(LocalDateTime.now())
                            .createdBy(createdBy)
                            .instrument(instrument)
                            .build())
                    .collect(Collectors.toList());
            configsToUse.addAll(defaultConfigs);
        }

        instrument.setConfigurations(configsToUse);

        // 3. Xử lý REAGENTS trước khi save instrument
        List<InstrumentReagents> reagentLinks = new ArrayList<>();

        if (request.getReagents() != null && !request.getReagents().isEmpty()) {

            for (CreateInstrumentReagentRequest r : request.getReagents()) {

                Reagent reagentEntity = reagentsRepository.findById(r.getReagentId())
                        .orElseThrow(() -> new WarehouseException(ErrorCode.REAGENT_NOT_FOUND));

                boolean consumed = reagentInventoryService.consumeReagentForInstrument(
                        r.getReagentId(),
                        BigDecimal.valueOf(r.getQuantity()),
                        null,  // instrument chưa persist nên chưa có ID
                        createdBy.toString()
                );

                if (!consumed) {
                    throw new WarehouseException(ErrorCode.REAGENT_NOT_ENOUGH);
                }

                InstrumentReagentId id = new InstrumentReagentId(null, reagentEntity.getReagentId());

                reagentLinks.add(
                        InstrumentReagents.builder()
                                .id(id)
                                .instrument(instrument)
                                .reagent(reagentEntity)
                                .quantity(r.getQuantity())
                                .assignedAt(LocalDateTime.now())
                                .build()
                );
            }
        }

        // 4. Giờ mới SAVE instrument (tất cả đã OK)
        Instrument saved = instrumentsRepository.save(instrument);

        // 5. Lưu links reagent sau khi instrument đã có ID
        for (InstrumentReagents link : reagentLinks) {
            link.getId().setInstrumentId(saved.getInstrumentId());
            instrumentReagentsRepository.save(link);
        }

        // 6. Publish event
        eventPublisher.publishEvent(
                "instrument",
                saved.getInstrumentId().toString(),
                "INSTRUMENT_CREATED",
                Map.of(
                        "name", saved.getName(),
                        "serialNumber", saved.getSerialNumber(),
                        "instrumentId", saved.getInstrumentId(),
                        "performedBy", saved.getCreatedBy()
                )
        );

        return saved;
    }

    @Override
    public List<Instrument> getInstruments() {
        List<Instrument> instruments = instrumentsRepository.findAll();
        if (instruments.isEmpty()) {
            throw new WarehouseException(ErrorCode.NO_DATA);
        }
        return instruments;
    }

    @Override
    public Instrument getInstrumentById(UUID instrumentId) {
        return instrumentsRepository.findById(instrumentId)
                .orElseThrow(() -> new WarehouseException(ErrorCode.INSTRUMENT_NOT_FOUND));
    }

    @Override
    public List<Instrument> getInstrumentsByStatus(Status status) {
        return instrumentsRepository.findByStatus(status);
    }

    @Override
    public List<Instrument> getInstrumentsByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return instrumentsRepository.findByCreatedAtBetween(start, end);
    }

    @Override
    public Instrument editInstrumentStatus(UUID id, Status newStatus, String reason, UUID updatedBy) {
        Instrument instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.INSTRUMENT_NOT_FOUND));

        if (newStatus.equals(Status.ERROR)) {
            instrument.setErrorMessage(reason);
        }

        instrument.setStatus(newStatus);
        instrument.setUpdatedBy(updatedBy);
        instrument.setUpdatedAt(LocalDateTime.now());

        Instrument saved = instrumentsRepository.save(instrument);
        eventPublisher.publishEvent("instrument", saved.getInstrumentId().toString(), "INSTRUMENT_UPDATED",
                Map.of("newStatus", newStatus.toString(),
                        "reason", reason,
                        "performedBy", updatedBy,
                        "instrumentId", saved.getInstrumentId()));
        return saved;
    }

    @Override
    public String checkInstrumentStatus(UUID id) {
        Instrument instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.INSTRUMENT_NOT_FOUND));

        if (instrument.getStatus() == Status.ERROR) {
            recheckInstrumentStatus(id);
            instrument = instrumentsRepository.findById(id)
                    .orElseThrow(() -> new WarehouseException(ErrorCode.INSTRUMENT_NOT_FOUND));

            if (instrument.getStatus() == Status.ERROR) {
                String errorDetail = instrument.getErrorMessage() != null
                        ? instrument.getErrorMessage()
                        : "Instrument still in error state after recheck.";
                return "Instrument " + instrument.getName()
                        + " remains in ERROR after recheck. Detail: " + errorDetail;
            }
        }

        return "Instrument " + instrument.getName() +
                " is currently in status: " + instrument.getStatus();
    }

    private boolean performHardwareDiagnostic(Instrument instrument) {
        return Math.random() > 0.2;
    }

    private boolean performNetworkCheck(Instrument instrument) {
        return Math.random() > 0.1;
    }

    @Override
    public void recheckInstrumentStatus(UUID id) {
        Instrument instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.INSTRUMENT_NOT_FOUND));

        log.info("[Recheck] Starting recheck for instrument: {}", instrument.getName());
        try {
            boolean hardwareOk = performHardwareDiagnostic(instrument);
            boolean networkOk = performNetworkCheck(instrument);

            if (hardwareOk && networkOk) {
                instrument.setStatus(Status.READY);
                instrument.setErrorMessage(null);
                log.info("[Recheck] Instrument {} recovered and is READY.", instrument.getName());
            } else {
                instrument.setStatus(Status.ERROR);
                StringBuilder errorDetail = new StringBuilder();
                if (!hardwareOk) errorDetail.append("Hardware malfunction detected. ");
                if (!networkOk) errorDetail.append("Network connection issue. ");
                instrument.setErrorMessage(errorDetail.toString().trim());
                log.warn("[Recheck] Instrument {} still in ERROR: {}", instrument.getName(), errorDetail);
            }
            instrumentsRepository.save(instrument);
        } catch (Exception e) {
            instrument.setStatus(Status.ERROR);
            instrument.setErrorMessage("Unexpected error during recheck: " + e.getMessage());
            instrumentsRepository.save(instrument);
            log.error("[Recheck] Exception during recheck: {}", e.getMessage());
        }
    }

    @Override
    public Instrument activateInstrument(UUID id, UUID updatedBy) {
        Instrument instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.INSTRUMENT_NOT_FOUND));

        instrument.setStatus(Status.READY);
        instrument.setUpdatedBy(updatedBy);
        instrument.setUpdatedAt(LocalDateTime.now());

        Instrument saved = instrumentsRepository.save(instrument);
        eventPublisher.publishEvent("instrument", saved.getInstrumentId().toString(), "INSTRUMENT_ACTIVE",
                Map.of("status", "READY",
                        "performedBy", updatedBy,
                        "instrumentId", saved.getInstrumentId()));
        return saved;
    }

    @Override
    public Instrument deactivateInstrument(UUID id, UUID updatedBy) {
        Instrument instrument = instrumentsRepository.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.INSTRUMENT_NOT_FOUND));

        instrument.setStatus(Status.INACTIVE);
        instrument.setUpdatedBy(updatedBy);
        instrument.setUpdatedAt(LocalDateTime.now());

        Instrument saved = instrumentsRepository.save(instrument);
        eventPublisher.publishEvent("instrument", saved.getInstrumentId().toString(), "INSTRUMENT_INACTIVE",
                Map.of("status", "INACTIVE",
                        "performedBy", updatedBy,
                        "instrumentId", saved.getInstrumentId()));
        return saved;
    }

    @Override
    public int autoDeleteDeactivatedInstruments() {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        List<Instrument> oldInactive = instrumentsRepository.findAll().stream()
                .filter(i -> i.getStatus() == Status.INACTIVE
                        && i.getUpdatedAt() != null
                        && i.getUpdatedAt().isBefore(threeMonthsAgo))
                .toList();

        for (Instrument i : oldInactive) {
            instrumentsRepository.deleteById(i.getInstrumentId());
            eventPublisher.publishEvent("instrument", i.getInstrumentId().toString(), "INSTRUMENT_AUTO_DELETED",
                    Map.of("deletedAt", LocalDateTime.now(),
                            "instrumentId", i.getInstrumentId(),
                            "reason", "Auto-deleted after 3 months of inactivity"
                    ));
        }

        return oldInactive.size();
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduleAutoDeleteDeactivatedInstruments() {
        int count = autoDeleteDeactivatedInstruments();
        log.info("Auto deleted {} inactive instruments older than 3 months", count);
    }

    private Map<String, Object> buildPayload(Instrument saved) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", saved.getInstrumentId());
        payload.put("name", saved.getName());
        payload.put("model", saved.getModel());
        payload.put("serialNumber", saved.getSerialNumber());
        payload.put("status", saved.getStatus());
        payload.put("createdAt", saved.getCreatedAt());
        payload.put("updatedAt", saved.getUpdatedAt());
        payload.put("configurations", saved.getConfigurations());
        payload.put("reagents", saved.getReagent());
        return payload;
    }
}
