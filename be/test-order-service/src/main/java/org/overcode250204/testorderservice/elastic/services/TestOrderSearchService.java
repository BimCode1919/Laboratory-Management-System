package org.overcode250204.testorderservice.elastic.services;

import org.overcode250204.testorderservice.elastic.documents.TestOrderDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Interface chính xử lý nghiệp vụ Search cho TestOrders
 */
public interface TestOrderSearchService {
    Page<TestOrderDocument> searchOrders(String name,
                                         String status,
                                         String testType,
                                         String priority,
                                         String barCode,
                                         String patientCode,
                                         String medicalRecordId,
                                         LocalDate createdAtFrom,
                                         LocalDate createdAtTo,
                                         String resultParamName,
                                         String resultStatus,
                                         Pageable pageable);

    // pageable được cố định bên trong service impl
    Page<TestOrderDocument> getSuggestions(String query);
}