package org.overcode250204.patientservice.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.overcode250204.patientservice.configs.aesgcm.EncryptedField;
import org.overcode250204.patientservice.configs.aesgcm.EncryptionConverter;
import org.overcode250204.patientservice.enums.Gender;


import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity(name = "patient")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "patient_id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable=false, unique=true)
    private UUID patientCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "age")
    private int age;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "phone", unique = true)
    @Convert(converter = EncryptionConverter.class)
    private String phone;

    @Column(name = "cognito_sub")
    private String cognitoSub;

    @Column(name = "email", unique = true)
    @Convert(converter = EncryptionConverter.class)
    private String email;

    @Column(name = "address")
    @Convert(converter = EncryptionConverter.class)
    private String address;

    @Column(name = "email_hash")
    private String emailHash;

    @Column(name = "phone_hash")
    private String phoneHash;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;


    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @JsonManagedReference("patient-history")
    @OneToMany(mappedBy = "patient")
    private List<MedicalRecordHistory> medicalRecordHistories;



    @PrePersist
    protected void onCreate() {
        if (this.patientCode == null) {
            this.patientCode = UUID.randomUUID();
        }


        this.isDeleted = false;

    }
}
