package org.overcode250204.instrumentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstalledReagent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "installed_reagent_id", unique = true, nullable = false)
    private UUID installedReagentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "reagent_id", nullable = false)
    private UUID reagentId;

    private String reagentName;

    private String lotNumber;

    private String vendorName;

    private LocalDate expirationDate;

    @Column(name = "quantity_remaining", columnDefinition = "Decimal(18,4)")
    private Double quantityRemaining;

    private String unit;

    private String status;

    private LocalDateTime installedAt;

    private UUID installedBy;

    private LocalDateTime lastCheckedAt;

    // indicate whether this installed reagent is actively used for test consumption
    @Column(name = "in_use", nullable = false, columnDefinition = "boolean default false")
    private boolean inUse;

    private UUID removedBy;

    private LocalDateTime uninstalledAt;


}
