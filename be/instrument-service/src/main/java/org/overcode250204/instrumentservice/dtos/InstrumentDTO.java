package org.overcode250204.instrumentservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.overcode250204.instrumentservice.enums.InstrumentMode;
import org.overcode250204.instrumentservice.enums.InstrumentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstrumentDTO {
    private UUID id;
    private String instrumentCode;
    private String name;
    private String model;
    private String serialNumber;
    private String location;
    private InstrumentStatus status;
    private InstrumentMode mode;
    private Boolean isOnline;
    private String configVersion;
    private LocalDateTime lastConfigSyncAt;
    private List<ConfigurationDTO> configurations;
}
