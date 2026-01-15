package org.overcode250204.patientservice.services;

import org.overcode250204.patientservice.documents.MedicalRecordDetailDocument;

import java.time.Instant;
import java.util.Optional;

public interface MedicalRecordDetailService {
    Optional<?> getDetailById(String recordId);
    MedicalRecordDetailDocument getFilteredDetail(String recordId, Instant startDate, Instant endDate, String testType, String instrumentUsed);

    MedicalRecordDetailDocument getFilteredPatientDetail(String sub, Instant startDate, Instant endDate, String testType, String instrumentUsed);
}
