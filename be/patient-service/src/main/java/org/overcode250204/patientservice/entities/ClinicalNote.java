package org.overcode250204.patientservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.overcode250204.patientservice.configs.aesgcm.EncryptionConverter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clinical_note")
public class ClinicalNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "note_id")
    private UUID id;

    @Column(name = "noted_by", nullable = false)
    private UUID notedBy;

    @Column(name = "note", columnDefinition = "TEXT")
    @Convert(converter = EncryptionConverter.class)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @JsonBackReference("record-note")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicalRecordId", nullable = false)
    @ToString.Exclude
    private MedicalRecord medicalRecord;
}