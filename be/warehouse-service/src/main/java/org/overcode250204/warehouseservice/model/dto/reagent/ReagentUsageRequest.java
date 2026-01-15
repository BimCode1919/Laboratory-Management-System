package org.overcode250204.warehouseservice.model.dto.reagent;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReagentUsageRequest {
    private UUID reagentId;
    private BigDecimal quantityUsed;
    private String usedBy;
    private String action;
}
