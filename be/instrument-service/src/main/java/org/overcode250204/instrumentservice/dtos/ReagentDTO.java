package org.overcode250204.instrumentservice.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.overcode250204.instrumentservice.entity.InstalledReagent;
import org.overcode250204.instrumentservice.entity.Instrument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReagentDTO {
    private UUID installedReagentId;
    private UUID reagentId;
    private String reagentName;
    private String lotNumber;
    private String vendorName;
    private Double quantityRemaining;
    private String unit;
    private LocalDate expirationDate;
    private boolean inUse;
    private LocalDateTime installedAt;

    private String status;
    private LocalDateTime lastCheckedAt;
    private LocalDateTime uninstalledAt;

    public ReagentDTO(InstalledReagent installedReagent) {
        this.installedReagentId = installedReagent.getInstalledReagentId();
        this.reagentId = installedReagent.getReagentId();
        this.reagentName = installedReagent.getReagentName();
        this.lotNumber = installedReagent.getLotNumber();
        this.vendorName = installedReagent.getVendorName();
        this.quantityRemaining = installedReagent.getQuantityRemaining();
        this.unit = installedReagent.getUnit();
        this.expirationDate = LocalDate.from(installedReagent.getExpirationDate());
        this.inUse = installedReagent.isInUse();
        this.installedAt = installedReagent.getInstalledAt();
        this.status = installedReagent.getStatus();
        this.lastCheckedAt = installedReagent.getLastCheckedAt();
        this.uninstalledAt = installedReagent.getUninstalledAt();
    }
}
