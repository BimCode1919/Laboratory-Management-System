package org.overcode250204.patientservice.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Document(indexName = "medical_record_detail", createIndex = false)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDetailDocument {

    @Id
    private String id;

    private String patientId;
    private String patientCode;
    private String fullName;
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd")
    private String dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    private String address;

    private String lastTestDate;
    private String status;

    private List<TestRecordInfo> testRecords;
    private List<ClinicalNoteInfo> clinicalNotes;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestRecordInfo {
        private String testOrderId;
        @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSX||epoch_millis")
        private String testCompletedAt;
        private String interpretation;

        private Map<String, Object> instrumentDetails;

        private Map<String, Object> reagentDetails;

        private List<Map<String, Object>> testResults;

        private String status;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClinicalNoteInfo {
        private String note;
        @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSX||epoch_millis")
        private String createdAt;
        private UUID notedBy;
    }
}
