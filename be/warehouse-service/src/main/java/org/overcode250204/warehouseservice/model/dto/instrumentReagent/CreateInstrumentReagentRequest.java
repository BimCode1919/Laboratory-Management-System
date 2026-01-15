package org.overcode250204.warehouseservice.model.dto.instrumentReagent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInstrumentReagentRequest {
    private UUID reagentId;   // ID của reagent trong DB
    private Double quantity;
}

