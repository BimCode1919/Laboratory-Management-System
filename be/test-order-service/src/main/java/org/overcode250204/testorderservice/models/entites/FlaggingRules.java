package org.overcode250204.testorderservice.models.entites;

import jakarta.persistence.*;
import lombok.*;
import org.overcode250204.testorderservice.models.enums.Gender;
import org.overcode250204.testorderservice.models.enums.TestOrderType;

import java.util.UUID;

@Entity
@Table(name = "flagging_rules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parameter_name", "unit", "gender"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlaggingRules {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "test_type")
    @Enumerated(EnumType.STRING)
    private TestOrderType testType;

    @Column(name = "parameter_name", nullable = false, length = 100)
    private String parameterName;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "gender", length = 20)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "normal_low")
    private Double normalLow;

    @Column(name = "normal_high")
    private Double normalHigh;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_activated")
    private Boolean isActivated = true;

    // Kiểm tra ràng buộc CHECK (normal_low < normal_high)
    @PrePersist
    @PreUpdate
    public void validateNormalRange() {
        if (normalLow != null && normalHigh != null && normalLow >= normalHigh) {
            throw new IllegalArgumentException("normal_low must be less than normal_high");
        }
    }
}