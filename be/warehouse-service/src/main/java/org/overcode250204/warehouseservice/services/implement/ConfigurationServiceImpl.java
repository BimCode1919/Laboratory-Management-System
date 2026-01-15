package org.overcode250204.warehouseservice.services.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.warehouseservice.events.publishers.MonitoringEventPublisher;
import org.overcode250204.warehouseservice.exceptions.ErrorCode;
import org.overcode250204.warehouseservice.exceptions.WarehouseException;
import org.overcode250204.warehouseservice.model.dto.configuration.ConfigurationDTO;
import org.overcode250204.warehouseservice.model.dto.configuration.CreateConfigurationRequest;
import org.overcode250204.warehouseservice.model.entities.Configuration;
import org.overcode250204.warehouseservice.model.entities.Instrument;
import org.overcode250204.warehouseservice.repositories.ConfigurationRepository;
import org.overcode250204.warehouseservice.repositories.InstrumentsRepository;
import org.overcode250204.warehouseservice.services.interfaces.ConfigurationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configRepo;
    private final InstrumentsRepository instrumentRepo;
    private final MonitoringEventPublisher monitoringEventPublisher;

    @Override
    public Configuration createConfiguration(CreateConfigurationRequest request, UUID createdBy) {
        if (request == null) {
            throw new WarehouseException(ErrorCode.INVALID_REQUEST);
        }
        Configuration config = Configuration.builder()
                .configName(request.getConfigName())
                .configKey(request.getConfigKey())
                .configValue(request.getConfigValue())
                .defaultValue(request.getDefaultValue())
                .description(request.getDescription())
                .isGlobal(request.isGlobal())
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();

        if (request.getInstrumentId() != null) {
            Instrument instrument = instrumentRepo.findById(UUID.fromString(request.getInstrumentId()))
                    .orElseThrow(() -> new WarehouseException(ErrorCode.INSTRUMENT_NOT_FOUND));
            config.setInstrument(instrument);
        }

        Configuration saved = configRepo.save(config);
        monitoringEventPublisher.publishEvent("CONFIGURATION_CREATED", saved.getConfigId().toString(), "CONFIGURATION_CREATED",
                Map.of("name", saved.getConfigName(),
                        "configKey", saved.getConfigKey(),
                        "performedBy", createdBy.toString()));
        return saved;
    }

    @Override
    public Configuration updateConfiguration(UUID id, CreateConfigurationRequest request, UUID updatedBy) {
        Configuration existing = configRepo.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.CONFIG_NOT_FOUND));
        if (request == null) {
            throw new WarehouseException(ErrorCode.INVALID_REQUEST);
        }

        existing.setConfigName(request.getConfigName());
        existing.setConfigKey(request.getConfigKey());
        existing.setConfigValue(request.getConfigValue());
        existing.setDefaultValue(request.getDefaultValue());
        existing.setDescription(request.getDescription());
        existing.setIsGlobal(request.isGlobal());
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(updatedBy);

        Configuration saved = configRepo.save(existing);
        monitoringEventPublisher.publishEvent("configuration", saved.getConfigId().toString(), "CONFIGURATION_UPDATED",
                Map.of("name", saved.getConfigName(), "configKey", saved.getConfigKey(),
                        "performedBy", updatedBy.toString()));

        return saved;
    }

    @Override
    public List<ConfigurationDTO> getAllConfigurations() {
        return configRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ConfigurationDTO getConfigurationById(UUID id) {
        Configuration config = configRepo.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.CONFIG_NOT_FOUND));
        return toDTO(config);
    }

    @Override
    public void deleteConfiguration(UUID id, UUID deletedBy) {
        Configuration existing = configRepo.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.CONFIG_NOT_FOUND));
        monitoringEventPublisher.publishEvent("configuration", existing.getConfigId().toString(), "CONFIGURATION_DELETED",
                Map.of("name", existing.getConfigName(), "configKey", existing.getConfigKey(),
                        "performedBy", deletedBy.toString()));


        configRepo.deleteById(id);
    }

    @Override
    public List<ConfigurationDTO> cloneGlobalConfigsToInstrument(UUID instrumentId, UUID updatedBy) {
        Instrument instrument = instrumentRepo.findById(instrumentId)
                .orElseThrow(() -> new WarehouseException(ErrorCode.INSTRUMENT_NOT_FOUND));

        List<Configuration> globalConfigs = configRepo.findAll()
                .stream()
                .filter(c -> c.getIsGlobal() != null && c.getIsGlobal())
                .toList();

        if (globalConfigs.isEmpty()) {
            throw new WarehouseException(ErrorCode.NO_GLOBAL_CONFIG_FOUND);
        }

        List<Configuration> clonedConfigs = globalConfigs.stream()
                .map(global -> Configuration.builder()
                        .configName(global.getConfigName())
                        .configKey(global.getConfigKey())
                        .configValue(global.getDefaultValue())
                        .defaultValue(global.getDefaultValue())
                        .description(global.getDescription())
                        .isGlobal(false)
                        .createdAt(LocalDateTime.now())
                        .updatedBy(updatedBy)
                        .instrument(instrument)
                        .build())
                .toList();

        configRepo.saveAll(clonedConfigs);
        log.info("Cloned {} configurations for instrument {}", clonedConfigs.size(), instrument.getName());
        return clonedConfigs.stream().map(this::toDTO).toList();
    }

    private Map<String, Object> buildPayload(Configuration saved) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", saved.getConfigId());
        payload.put("configName", saved.getConfigName());
        payload.put("configKey", saved.getConfigKey());
        payload.put("configValue", saved.getConfigValue());
        payload.put("defaultValue", saved.getDefaultValue());
        payload.put("description", saved.getDescription());
        payload.put("isGlobal", saved.getIsGlobal());
        payload.put("instrumentId", saved.getInstrument() != null ? saved.getInstrument().getInstrumentId() : null);
        payload.put("createdBy", saved.getCreatedBy());
        payload.put("createdAt", saved.getCreatedAt());
        payload.put("updatedAt", saved.getUpdatedAt());
        return payload;
    }

    private ConfigurationDTO toDTO(Configuration c) {
        return ConfigurationDTO.builder()
                .configId(c.getConfigId())
                .configName(c.getConfigName())
                .configKey(c.getConfigKey())
                .configValue(c.getConfigValue())
                .defaultValue(c.getDefaultValue())
                .description(c.getDescription())
                .isGlobal(c.getIsGlobal() != null ? c.getIsGlobal() : false)
                .instrumentId(c.getInstrument() != null ? c.getInstrument().getInstrumentId() : null)
                .build();
    }
}
