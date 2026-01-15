package org.overcode250204.testorderservice.models.entites;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.overcode250204.testorderservice.models.enums.TestOrderPriority;
import org.overcode250204.testorderservice.models.enums.TestOrderStatus;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

import java.io.Serial;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "test_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "results"})
public class TestOrders {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_code", nullable = false, unique = true, length = 100)
    private String orderCode;

    @Column(name = "barcode", nullable = false, unique = true, length = 100)
    private String barCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = true)
    private TestOrderType testType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientReference patient;

    @Column(name = "medical_record_id")
    private UUID medicalRecordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private TestOrderPriority priority;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TestOrderStatus status;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_by", nullable = true)
    private UUID reviewedBy;

    @Column(name = "reviewed_at", nullable = true)
    private LocalDateTime reviewedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @JsonIgnoreProperties("testOrder")
    @OneToMany(mappedBy = "testOrder")
    private List<TestResults> results;

    @OneToMany(mappedBy = "testOrder")
    private List<TestComments> comments;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}