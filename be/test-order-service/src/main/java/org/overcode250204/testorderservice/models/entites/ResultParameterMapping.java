package org.overcode250204.testorderservice.models.entites;

import jakarta.persistence.*;
import lombok.*;
import org.overcode250204.testorderservice.models.enums.TestOrderType;

import java.util.UUID;

@Entity
@Table(name = "result_parameter_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"external_param_name", "internal_param_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultParameterMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "test_type")
    @Enumerated(EnumType.STRING)
    private TestOrderType testType;

    @Column(name = "external_param_name", nullable = false, length = 100)
    private String externalParamName;

    @Column(name = "internal_param_name", nullable = false, length = 100)
    private String internalParamName;

    @Column(name = "data_source", nullable = false, length = 50)
    private String dataSource;

    @Column(name = "is_activated")
    private Boolean isActivated = true;
}