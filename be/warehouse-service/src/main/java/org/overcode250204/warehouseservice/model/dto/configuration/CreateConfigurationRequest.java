package org.overcode250204.warehouseservice.model.dto.configuration;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConfigurationRequest {
    private String configName;
    private String configKey;
    private String configValue;
    private String defaultValue;
    private String description;
    private boolean isGlobal;
    private String instrumentId; // optional
}
