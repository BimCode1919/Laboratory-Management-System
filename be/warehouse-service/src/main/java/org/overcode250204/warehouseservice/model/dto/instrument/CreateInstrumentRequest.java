package org.overcode250204.warehouseservice.model.dto.instrument;

import lombok.*;
import org.overcode250204.warehouseservice.model.dto.instrumentReagent.CreateInstrumentReagentRequest;
import org.overcode250204.warehouseservice.model.enums.Status;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInstrumentRequest {
    private String name;
    private String serialNumber;
    private String model;
    private Status status;
    private UUID cloneFromInstrumentId;
    private List<UUID> configurationIds;
    private List<CreateInstrumentReagentRequest> reagents;
}
