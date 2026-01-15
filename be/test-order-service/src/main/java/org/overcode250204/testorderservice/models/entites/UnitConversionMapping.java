package org.overcode250204.testorderservice.models.entites;

import jakarta.persistence.*;
import lombok.*;
import org.overcode250204.testorderservice.models.enums.TestOrderType;

import java.util.UUID;

@Entity
@Table(name = "unit_conversion_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"test_type", "source_unit", "target_unit"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitConversionMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "test_type")
    @Enumerated(EnumType.STRING)
    private TestOrderType testType;

    @Column(name = "data_source", length = 50)
    private String dataSource;

    @Column(name = "source_unit", nullable = false, length = 50)
    private String sourceUnit;

    @Column(name = "target_unit", nullable = false, length = 50)
    private String targetUnit;

    @Column(name = "formula", columnDefinition = "TEXT")
    private String formula;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_activated")
    private Boolean isActivated = true;
}