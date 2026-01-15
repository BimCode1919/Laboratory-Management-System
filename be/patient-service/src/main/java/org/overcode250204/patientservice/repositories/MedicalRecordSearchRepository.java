package org.overcode250204.patientservice.repositories;

import org.overcode250204.patientservice.documents.MedicalRecordDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

public interface MedicalRecordSearchRepository extends ElasticsearchRepository<MedicalRecordDocument, String> {

    Page<MedicalRecordDocument> findByFullNameContainingIgnoreCaseOrPatientCodeContainingIgnoreCase(
            String fullName, String patientCode, Pageable pageable);

    Page<MedicalRecordDocument> findByLastTestDateBetween(Instant start, Instant end, Pageable pageable);
}
