package org.overcode250204.instrumentservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagentEstimateDTO {
    private boolean sufficient;
    private int samplesRequested;
    private Map<String, Double> requiredPerReagent;
    private Map<String, Double> availablePerReagent;
    private Map<String, Double> shortfallPerReagent;
    private Map<String, Double> runsLeftPerReagent;
    private Integer estimatedRunsPossible;
    // optional details about installed reagent batches that contribute to availability
    private Map<String, List<InstalledReagentDetail>> installedDetailsPerReagent;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstalledReagentDetail {
        private String lotNumber;
        private Double quantityRemaining;
        private boolean inUse;
        private String unit;
        // new fields to display expiration info
        private LocalDate expirationDate;
        private boolean expired;
    }
}
