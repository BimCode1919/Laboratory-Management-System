package org.overcode250204.patientservice.documents;

import lombok.*;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDate;

@Document(indexName = "medical_records", createIndex = false)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordDocument {
    @Id
    private String id;
    private String patientId;
    private String patientCode;
    private String fullName;
    private String dateOfBirth;
    private String lastTestDate;
    private String status;
    private String testType;

    private Object instrumentUsed;
}
