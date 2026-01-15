package org.overcode250204.patientservice.entities;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity(name = "medical_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.Where(clause = "is_deleted = false")
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "medical_record_id")
    private UUID id;

    @Column(name = "last_test_date")
    private Instant lastTestDate;

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false, referencedColumnName = "patient_id")
    private Patient patient;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "visit_date")
    private Instant visitDate;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @JsonManagedReference("record-note")
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClinicalNote> clinicalNotes;

    @JsonManagedReference("record-test")
    @OneToMany(mappedBy = "medicalRecord")
    private List<TestRecord> testRecords;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.isDeleted = false;
    }
}