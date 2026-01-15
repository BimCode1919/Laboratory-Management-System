package org.overcode250204.warehouseservice.services.interfaces;

import org.overcode250204.warehouseservice.model.dto.configuration.ConfigurationDTO;
import org.overcode250204.warehouseservice.model.dto.configuration.CreateConfigurationRequest;
import org.overcode250204.warehouseservice.model.entities.Configuration;

import java.util.List;
import java.util.UUID;

public interface ConfigurationService {
    Configuration createConfiguration(CreateConfigurationRequest request, UUID createdBy);

    Configuration updateConfiguration(UUID id, CreateConfigurationRequest request, UUID updatedBy);

    List<ConfigurationDTO> getAllConfigurations();

    ConfigurationDTO getConfigurationById(UUID id);

    void deleteConfiguration(UUID id, UUID deletedBy);

    List<ConfigurationDTO> cloneGlobalConfigsToInstrument(UUID instrumentId, UUID updatedBy);


}
