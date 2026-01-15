package org.overcode250204.warehouseservice.model.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reagent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reagent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "reagent_id")
    private UUID reagentId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "catalog_number", nullable = false, length = 50)
    private String catalogNumber;

    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer;

    @Column(name = "cas_number", length = 20)
    private String casNumber;
    @Column(name = "created_by", length = 50)
    private UUID createdBy;

    @Column(name = "updated_by", length = 50)
    private UUID updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    // Relationships
    @OneToMany(mappedBy = "reagent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "reagent-instrument")
    private List<InstrumentReagents> instrumentLink;

    @OneToMany(mappedBy = "reagent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReagentSupplyHistory> supplyHistorie;

    @OneToMany(mappedBy = "reagent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReagentUsageHistory> usageHistorie;
}
