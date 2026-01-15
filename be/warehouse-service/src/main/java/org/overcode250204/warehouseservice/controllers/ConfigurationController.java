package org.overcode250204.warehouseservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.warehouseservice.model.dto.configuration.ConfigurationDTO;
import org.overcode250204.warehouseservice.model.dto.configuration.CreateConfigurationRequest;
import org.overcode250204.warehouseservice.model.entities.Configuration;
import org.overcode250204.warehouseservice.services.interfaces.ConfigurationService;
import org.overcode250204.warehouseservice.utils.AuthUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/configurations")
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationService configurationService;
    private static final String SERVICE_NAME = "warehouse-service";

    @PostMapping
    @PreAuthorize("hasAuthority('CONFIG_CREATE')")
    public ResponseEntity<BaseResponse<Configuration>> createConfig(@RequestBody CreateConfigurationRequest request) {
        UUID createdBy = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        Configuration config = configurationService.createConfiguration(request, createdBy);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, config));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFIG_UPDATE')")
    public ResponseEntity<BaseResponse<Configuration>> updateConfig(
            @PathVariable UUID id,
            @RequestBody CreateConfigurationRequest request) {
        UUID updatedBy = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        Configuration updated = configurationService.updateConfiguration(id, request, updatedBy);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, updated));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONFIG_READ')")
    public ResponseEntity<BaseResponse<List<ConfigurationDTO>>> getAllConfigs() {
        List<ConfigurationDTO> configs = configurationService.getAllConfigurations();
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, configs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFIG_UPDATE')")
    public ResponseEntity<BaseResponse<ConfigurationDTO>> getConfigById(@PathVariable UUID id) {
        ConfigurationDTO config = configurationService.getConfigurationById(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, config));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFIG_DELETE')")
    public ResponseEntity<BaseResponse<String>> deleteConfig(@PathVariable UUID id) {
        UUID deletedBy = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        configurationService.deleteConfiguration(id, deletedBy);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, "Configuration deleted successfully"));
    }

    @PostMapping("/clone/{instrumentId}")
    public ResponseEntity<BaseResponse<List<ConfigurationDTO>>> cloneConfigsToInstrument(
            @PathVariable UUID instrumentId) {
        UUID updatedBy = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        List<ConfigurationDTO> cloned = configurationService.cloneGlobalConfigsToInstrument(instrumentId, updatedBy);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, cloned));
    }

}
