package org.overcode250204.patientservice.dtos;

import lombok.Builder;
import lombok.Data;
import org.overcode250204.patientservice.entities.ClinicalNote;
import org.overcode250204.patientservice.entities.Patient;
import org.overcode250204.patientservice.entities.TestRecord;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MedicalRecordDTO {
    private UUID recordId;
    private UUID recordCode;
    private Patient patient;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private Instant visitDate;
    private Instant createdAt;
    private UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private int totalNotes;
    private List<ClinicalNote> clinicalNotes;
    private List<TestRecord> testRecords;
}