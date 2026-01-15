package org.overcode250204.warehouseservice.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reagent_usage_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagentUsageHistory {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "usage_id", updatable = false, nullable = false)
    private UUID usageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reagent_id", nullable = false)
    private Reagent reagent;

    @Column(name = "quantity_used", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantityUsed;

    @Column(name = "usage_date", nullable = false)
    private LocalDateTime usageDate = LocalDateTime.now();

    @Column(name = "used_by", nullable = false, length = 50)
    private String usedBy;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "note", length = 1000)
    private String note;

    @Version
    @Column(name = "version")
    private Long version;
}

