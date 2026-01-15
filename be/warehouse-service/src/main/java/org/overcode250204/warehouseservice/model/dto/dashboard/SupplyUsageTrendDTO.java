package org.overcode250204.warehouseservice.model.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyUsageTrendDTO {
    private String date;
    private Double supplyQuantity;
    private Double usageQuantity;
}
