package org.overcode250204.patientservice.services;

import org.overcode250204.patientservice.documents.MedicalRecordDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.time.Instant;

public interface MedicalRecordSearchService {
    Page<MedicalRecordDocument> search(
            String keyword,
            String startDate,
            String endDate,
            String sortBy,
            Sort.Direction sortDirection,
            int page,
            int size);
}
