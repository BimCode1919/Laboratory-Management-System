package org.overcode250204.patientservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.overcode250204.patientservice.configs.aesgcm.EncryptionConverter;
import org.overcode250204.patientservice.enums.TestOrderType;
import org.overcode250204.patientservice.enums.TestRecordStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "test_record")
@Getter
@Setter
@NoArgsConstructor
public class TestRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonBackReference("record-test")
    @ManyToOne
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;


    @Column(name = "test_order_id", unique = true)
    private UUID testOrderId;

    @Column(name = "test_completed_at")
    private Instant testCompletedAt;


    @Column(name = "test_results_json", columnDefinition = "TEXT")
    @Convert(converter = EncryptionConverter.class)
    private String testResultsJson;


    @Column(name = "instrument_details_json", columnDefinition = "TEXT")
    private String instrumentDetailsJson;


    @Column(name = "reagent_details_json", columnDefinition = "TEXT")
    private String reagentDetailsJson;

    @Column(name = "interpretation", columnDefinition = "TEXT")
    private String interpretation;

    @Enumerated(EnumType.STRING)
    private TestRecordStatus status;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    private TestOrderType  testOrderType;


}
