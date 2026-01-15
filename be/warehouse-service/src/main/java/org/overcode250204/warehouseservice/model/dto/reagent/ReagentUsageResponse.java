package org.overcode250204.warehouseservice.model.dto.reagent;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagentUsageResponse {

    private UUID usageId;
    private UUID reagentId;
    private String reagentName;
    private BigDecimal quantityUsed;
    private String action;
    private String usedBy;
    private LocalDateTime usageDate;
}
