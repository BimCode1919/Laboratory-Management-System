package org.overcode250204.testorderservice.elastic.mappers;

import org.overcode250204.testorderservice.elastic.documents.TestOrderDocument;
import org.overcode250204.testorderservice.elastic.documents.TestResultSubDocument;
import org.overcode250204.testorderservice.models.entites.PatientReference;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestOrderDocumentMapper {
    /**
     * Phương thức helper để map (sao chép từ TestOrderIndexingServiceImpl)
     * Đảm bảo logic map ở đây GIỐNG HỆT với logic map real-time.
     */
    public TestOrderDocument mapToDocument(TestOrders order, List<TestResults> results) {
        PatientReference patient = order.getPatient();

        List<TestResultSubDocument> resultSubDocuments;
        if (results != null && !results.isEmpty()) {
            resultSubDocuments = results.stream()
                    .map(this::mapToSubDocument)
                    .collect(Collectors.toList());
        } else {
            resultSubDocuments = Collections.emptyList();
        }

        return TestOrderDocument.builder()
                .id(order.getId().toString())
                .orderCode(order.getOrderCode())
                .barCode(order.getBarCode())
                .testType(order.getTestType())
                .medicalRecordId(order.getMedicalRecordId() != null ? order.getMedicalRecordId().toString() : null)
                .priority(order.getPriority())
                .status(order.getStatus())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt() : null)
                .reviewedAt(order.getReviewedAt() != null ? order.getReviewedAt(): null)
                .createdBy(order.getCreatedBy() != null ? order.getCreatedBy().toString() : null)
                .reviewedBy(order.getReviewedBy() != null ? order.getReviewedBy().toString() : null)

                // Dữ liệu phi chuẩn hóa từ Patient
                .patientId(patient != null && patient.getPatientId() != null ? patient.getPatientId().toString() : null)
                .patientCode(patient != null && patient.getPatientCode() != null ? patient.getPatientCode().toString() : null)
                .patientFullName(patient != null ? patient.getFullName() : "N/A")
                .patientDateOfBirth(patient != null ? patient.getDateOfBirth() : null)
                .patientGender(patient != null ? patient.getGender() : null)
                .patientPhoneNumber(patient != null ? patient.getPhoneNumber() : null)

                // Dữ liệu nested từ Results
                .results(resultSubDocuments)
                .build();
    }

    // Hàm helper để map một TestResults (Entity) sang TestResultSubDocument
    private TestResultSubDocument mapToSubDocument(TestResults result) {
        if (result == null) {
            return null;
        }
        return TestResultSubDocument.builder()
                .parameterName(result.getParameterName())
                .status(result.getStatus())
                .resultValue(result.getResultValue())
                .unit(result.getUnit())
                .build();
    }
}