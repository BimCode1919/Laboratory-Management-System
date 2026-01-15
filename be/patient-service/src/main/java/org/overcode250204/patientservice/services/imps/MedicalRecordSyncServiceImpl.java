package org.overcode250204.patientservice.services.imps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.patientservice.documents.MedicalRecordDetailDocument;
import org.overcode250204.patientservice.documents.MedicalRecordDocument;
import org.overcode250204.patientservice.entities.MedicalRecord;
import org.overcode250204.patientservice.entities.Patient;
import org.overcode250204.patientservice.entities.TestRecord;
import org.overcode250204.patientservice.repositories.MedicalRecordDetailRepository;
import org.overcode250204.patientservice.repositories.MedicalRecordSearchRepository;
import org.overcode250204.patientservice.services.MedicalRecordSyncService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordSyncServiceImpl implements MedicalRecordSyncService{

    private final MedicalRecordSearchRepository searchRepository;
    private final MedicalRecordDetailRepository detailRepository;
    private final ObjectMapper objectMapper;

    public void indexMedicalRecord(MedicalRecord record) {
        indexSummary(record);
        indexDetail(record);
    }

    private void indexSummary(MedicalRecord record) {
        Patient patient = record.getPatient();
        if (patient == null) {
            log.error("Cannot index summary: Patient data is missing for Record ID [{}]", record.getId());
            return;
        }
        AtomicReference<String> testType = new AtomicReference<>();
        AtomicReference<Object> instrumentUsed = new AtomicReference<>();

        if (record.getTestRecords() != null && !record.getTestRecords().isEmpty()) {
            record.getTestRecords().stream()
                    .filter(t -> t.getTestCompletedAt() != null)
                    .max(Comparator.comparing(TestRecord::getTestCompletedAt))
                    .ifPresent(latest -> {
                        Map<String, Object> details = parseJsonToMap(latest.getInstrumentDetailsJson());
                        instrumentUsed.set(details);
                        testType.set(latest.getTestOrderType().toString());
                    });
        }
        MedicalRecordDocument doc = new MedicalRecordDocument();
        doc.setId(record.getId().toString());
        doc.setPatientId(patient.getId().toString());
        doc.setInstrumentUsed(instrumentUsed.get() != null ? instrumentUsed.get() : Map.of());
        doc.setTestType(testType.get());
        doc.setPatientCode(patient.getPatientCode().toString());
        doc.setFullName(patient.getFullName());
        doc.setDateOfBirth(patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null);
        doc.setLastTestDate(record.getLastTestDate() != null ? record.getLastTestDate().toString() : null);
        try {
            log.info("Document BEFORE save: {}", objectMapper.writeValueAsString(doc));
        } catch (Exception e) {

        }
        searchRepository.save(doc);

        log.info("Indexed summary record [{}]", record.getId());
    }

    private void indexDetail(MedicalRecord record) {
        Patient patient = record.getPatient();

        List<TestRecord> testRecords = record.getTestRecords() != null
                ? record.getTestRecords()
                : List.of();

        List<MedicalRecordDetailDocument.TestRecordInfo> tests = testRecords.stream()
                .map(t -> MedicalRecordDetailDocument.TestRecordInfo.builder()
                        .testOrderId(t.getTestOrderId() != null ? t.getTestOrderId().toString() : null)
                        .testCompletedAt(t.getTestCompletedAt().toString())
                        .interpretation(t.getInterpretation())
                        .instrumentDetails(parseJsonToMap(t.getInstrumentDetailsJson()))
                        .reagentDetails(parseJsonToMap(t.getReagentDetailsJson()))
                        .testResults(parseJsonToList(t.getTestResultsJson()))
                        .status(t.getStatus().name())
                        .build())
                .toList();

        List<MedicalRecordDetailDocument.ClinicalNoteInfo> notes = record.getClinicalNotes() != null
                ? record.getClinicalNotes().stream()
                .map(n -> MedicalRecordDetailDocument.ClinicalNoteInfo.builder()
                        .note(n.getNote())
                        .createdAt(n.getCreatedAt().toString())
                        .notedBy(n.getNotedBy())
                        .build())
                .toList()
                : List.of();

        MedicalRecordDetailDocument detail = new MedicalRecordDetailDocument();
        detail.setId(record.getId().toString());
        detail.setLastTestDate(record.getLastTestDate() != null ? record.getLastTestDate().toString() : null);
        detail.setTestRecords(tests);
        detail.setClinicalNotes(notes);
        detail.setPatientId(patient.getId() != null ? patient.getId().toString() : null);
        detail.setPatientCode(patient.getPatientCode().toString());
        detail.setFullName(patient.getFullName());
        detail.setDateOfBirth(patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null);
        detail.setGender(patient.getGender() != null ? patient.getGender().name() : null);
        detail.setPhone(patient.getPhone());
        detail.setEmail(patient.getEmail());
        detail.setAddress(patient.getAddress());

        try {
            log.info("Document BEFORE save: {}", objectMapper.writeValueAsString(detail));
        } catch (Exception e) {

        }
        detailRepository.save(detail);
        log.info("Indexed detailed record [{}]", record.getId());
    }

    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON to Map. Data: {}. Error: {}", json, e.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> parseJsonToList(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON to List. Data: {}. Error: {}", json, e.getMessage());
            return null;
        }
    }
}
