package org.overcode250204.warehouseservice.model.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentReagentId implements Serializable {
    private UUID instrumentId;
    private UUID reagentId;
}

