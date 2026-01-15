package org.overcode250204.testorderservice.elastic.documents;

import lombok.*;
import org.overcode250204.testorderservice.models.enums.TestOrderPriority;
import org.overcode250204.testorderservice.models.enums.TestOrderStatus;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "test_orders_index", createIndex = false)
@Setting(settingPath = "elastic/analyzers.json")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOrderDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private String orderCode;

    @Field(type = FieldType.Keyword)
    private String barCode;

    @Field(type = FieldType.Keyword)
    private TestOrderType testType;

    @Field(type = FieldType.Keyword)
    private String medicalRecordId;

    @Field(type = FieldType.Keyword)
    private TestOrderPriority priority;

    @Field(type = FieldType.Keyword)
    private TestOrderStatus status;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String notes;

    @Field(type = FieldType.Date, format = DateFormat.strict_date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.strict_date_hour_minute_second)
    private LocalDateTime reviewedAt;

    @Field(type = FieldType.Keyword)
    private String createdBy;

    @Field(type = FieldType.Keyword)
    private String reviewedBy;

    // --- Denormalized Patient Data ---
    // Đây là phần phi chuẩn hóa từ PatientReference

    @Field(type = FieldType.Keyword)
    private String patientId;

    @Field(type = FieldType.Keyword)
    private String patientCode;

    //    @Field(type = FieldType.Text, analyzer = "vietnamese_analyzer") // Để tìm kiếm full-text
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "vietnamese_analyzer"), // <-- Trường chính
            otherFields = {
                    @InnerField(suffix = "suggest", type = FieldType.Text, analyzer = "autocomplete_analyzer") // <-- Trường con nGram
            }
    )
    private String patientFullName;

    @Field(type = FieldType.Date, format = DateFormat.strict_date)
    private LocalDate patientDateOfBirth;

    @Field(type = FieldType.Keyword)
    private String patientGender;

    @Field(type = FieldType.Keyword)
    private String patientPhoneNumber;

    // Test Results
    @Field(type = FieldType.Nested)
    private List<TestResultSubDocument> results;
}