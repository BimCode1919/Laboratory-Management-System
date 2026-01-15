package org.overcode250204.warehouseservice.model.dto.instrument;

import lombok.*;
import org.overcode250204.warehouseservice.model.enums.Status;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentResponse {
    private UUID instrumentId;
    private String name;
    private String serialNumber;
    private String model;
    private Status status;
    private String location;
    private String instrumentCode;
    private String configVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
