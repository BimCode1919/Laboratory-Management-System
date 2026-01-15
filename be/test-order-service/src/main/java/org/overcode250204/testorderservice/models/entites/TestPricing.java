package org.overcode250204.testorderservice.models.entites;

import jakarta.persistence.*;
import lombok.*;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import java.util.UUID;

@Entity
@Table(name = "test_pricing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TestOrderType testType;

    @Column(nullable = false)
    private Double price;
}
