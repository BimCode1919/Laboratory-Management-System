package org.overcode250204.patientservice.services.imps;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.patientservice.documents.MedicalRecordDetailDocument;
import org.overcode250204.patientservice.documents.MedicalRecordDetailDocument.TestRecordInfo;
import org.overcode250204.patientservice.entities.MedicalRecord;
import org.overcode250204.patientservice.entities.Patient;
import org.overcode250204.patientservice.exceptions.ErrorCode;
import org.overcode250204.patientservice.exceptions.PatientException;
import org.overcode250204.patientservice.repositories.MedicalRecordDetailRepository;
import org.overcode250204.patientservice.repositories.MedicalRecordRepository;
import org.overcode250204.patientservice.repositories.PatientRepository;
import org.overcode250204.patientservice.services.MedicalRecordDetailService;
import org.overcode250204.patientservice.services.MedicalRecordSyncService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordDetailServiceImpl implements MedicalRecordDetailService {

    private final MedicalRecordDetailRepository detailRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordSyncService medicalRecordSyncService;

    @Override
    public Optional<MedicalRecordDetailDocument> getDetailById(String recordId) {
        try {
            return detailRepository.findById(recordId);
        } catch (Exception e) {
            log.error("Error fetching medical record detail for id={}: {}", recordId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public MedicalRecordDetailDocument getFilteredDetail(
            String recordId,
            Instant startDate,
            Instant endDate,
            String testType,
            String instrumentUsed
    ) {

        Optional<MedicalRecordDetailDocument> opt = getDetailById(recordId);

        if (opt.isEmpty()) {
            throw new EntityNotFoundException("Medical record not found with ID: " + recordId);
        }

        MedicalRecordDetailDocument detail = opt.get();
        List<TestRecordInfo> filteredTests = applyFilters(detail.getTestRecords(), startDate, endDate, testType, instrumentUsed);

        detail.setTestRecords(filteredTests);
        log.info("[DETAIL] Filtered {} test records for recordId={}", filteredTests.size(), recordId);
        return detail;
    }

    @Override
    public MedicalRecordDetailDocument getFilteredPatientDetail(
            String sub,
            Instant startDate,
            Instant endDate,
            String testType,
            String instrumentUsed
    ) {
        Patient patient = patientRepository.findByCognitoSub(sub);
        if (patient == null) {
            throw new PatientException(ErrorCode.PATIENT_FIND_WITH_SUB_NOT_FOUND);
        }

        MedicalRecord medicalRecord = medicalRecordRepository.findByPatientId(patient.getId())
                .orElseThrow(() -> new EntityNotFoundException("Medical record not found for patient ID: " + patient.getId()));

        return getFilteredDetail(medicalRecord.getId().toString(), startDate, endDate, testType, instrumentUsed);
    }

    private List<TestRecordInfo> applyFilters(
            List<TestRecordInfo> sourceRecords,
            Instant startDate,
            Instant endDate,
            String testType,
            String instrumentUsed
    ) {
        if (sourceRecords == null || sourceRecords.isEmpty()) {
            return List.of();
        }

        return sourceRecords.stream()
                .filter(t -> filterByDate(t, startDate, endDate))
                .filter(t -> filterByTestType(t, testType))
                .filter(t -> filterByInstrument(t, instrumentUsed))
                .toList();
    }

    private boolean filterByDate(TestRecordInfo t, Instant startDate, Instant endDate) {
        if (startDate == null || endDate == null) return true;
        String dateStr = t.getTestCompletedAt();
        if (dateStr == null || dateStr.isBlank()) return false;
        try {
            Instant completedAt;
            if (dateStr.matches("-?\\d+")) {
                completedAt = Instant.ofEpochMilli(Long.parseLong(dateStr));
            } else {
                completedAt = Instant.parse(dateStr);
            }
            return completedAt != null &&
                    !completedAt.isBefore(startDate) &&
                    !completedAt.isAfter(endDate);
        } catch (Exception e) {
            throw new PatientException(ErrorCode.FAIL_TO_PARSE_COMPLETED_AT);
        }

    }

    private boolean filterByTestType(TestRecordInfo t, String testType) {
        if (testType == null || testType.isBlank()) return true;
        return t.getInterpretation() != null &&
                t.getInterpretation().toLowerCase().contains(testType.toLowerCase());
    }

    private boolean filterByInstrument(TestRecordInfo t, String instrumentUsed) {
        if (instrumentUsed == null || instrumentUsed.isBlank()) return true;

        Map<String, Object> instrumentMap = t.getInstrumentDetails();
        if (instrumentMap == null) return false;

        String name = (String) instrumentMap.getOrDefault("name", "");
        String code = (String) instrumentMap.getOrDefault("instrumentCode", "");

        String keyword = instrumentUsed.toLowerCase();
        return (name != null && name.toLowerCase().contains(keyword)) ||
                (code != null && code.toLowerCase().contains(keyword));
    }
}

