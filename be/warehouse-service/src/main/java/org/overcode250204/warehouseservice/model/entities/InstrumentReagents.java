package org.overcode250204.warehouseservice.model.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "instrument_reagents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentReagents {

    @EmbeddedId
    @Builder.Default
    private InstrumentReagentId id = new InstrumentReagentId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("instrumentId")
    @JoinColumn(name = "instrument_id", nullable = false)
    @JsonBackReference(value = "instrument-reagent")
    private Instrument instrument;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("reagentId")
    @JoinColumn(name = "reagent_id", nullable = false)
    @JsonBackReference(value = "reagent-instrument")
    private Reagent reagent;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();
}
