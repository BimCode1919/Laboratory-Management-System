package org.overcode250204.warehouseservice.model.dto.configuration;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationDTO {
    private UUID configId;
    private String configName;
    private String configKey;
    private String configValue;
    private String defaultValue;
    private String description;
    private boolean isGlobal;
    private UUID instrumentId;
}
