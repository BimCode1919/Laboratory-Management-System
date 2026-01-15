package org.overcode250204.warehouseservice.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagentStockLevelDTO {
    private String reagentName;
    private Double quantity;
}
