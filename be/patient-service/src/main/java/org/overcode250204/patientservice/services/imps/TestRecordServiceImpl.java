package org.overcode250204.patientservice.services.imps;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.patientservice.entities.MedicalRecord;
import org.overcode250204.patientservice.entities.TestRecord;
import org.overcode250204.patientservice.enums.TestOrderType;
import org.overcode250204.patientservice.enums.TestRecordStatus;
import org.overcode250204.patientservice.exceptions.ErrorCode;
import org.overcode250204.patientservice.exceptions.PatientException;
import org.overcode250204.patientservice.repositories.MedicalRecordRepository;
import org.overcode250204.patientservice.repositories.TestRecordRepository;
import org.overcode250204.patientservice.services.MedicalRecordSyncService;
import org.overcode250204.patientservice.services.TestRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestRecordServiceImpl implements TestRecordService {

    private final TestRecordRepository testRecordRepository;

    private final ObjectMapper objectMapper;

    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordSyncService medicalRecordSyncService;


    @Override
    @Transactional
    public void saveTestRecord(Map<String, Object> payload) {
        try {
            UUID medicalRecordId = UUID.fromString((String) payload.get("medicalRecordId"));
            UUID testOrderId = UUID.fromString((String) payload.get("testOrderId"));
            Instant completeAt = Instant.parse((String) payload.get("completedAt"));
            Object testResults = payload.get("results");
            Object instrumentDetails = payload.get("instrumentDetails");
            Object reagentDetails = payload.get("reagentDetails");
            String testType = (String) payload.get("testType");
            String status = (String) payload.get("status");
            TestOrderType  testOrderType = TestOrderType.valueOf(testType);
            TestRecordStatus statusEnum;

            statusEnum = TestRecordStatus.valueOf(status.toUpperCase());



            MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                    .orElseThrow(() -> new PatientException(ErrorCode.MEDICAL_RECORD_NOT_FOUND));

            TestRecord testRecord = new TestRecord();
            testRecord.setMedicalRecord(medicalRecord);
            testRecord.setTestCompletedAt(completeAt);
            testRecord.setTestOrderId(testOrderId);
            testRecord.setTestResultsJson(objectMapper.writeValueAsString(testResults));
            testRecord.setInstrumentDetailsJson(objectMapper.writeValueAsString(instrumentDetails));
            testRecord.setReagentDetailsJson(objectMapper.writeValueAsString(reagentDetails));
            testRecord.setTestOrderType(testOrderType);
            testRecord.setStatus(statusEnum);



            medicalRecord.getTestRecords().add(testRecord);

            testRecordRepository.save(testRecord);
            //Update state of medical record
            medicalRecord.setLastTestDate(completeAt);
            //medicalRecordRepository.save(medicalRecord);
            MedicalRecord updatedMedicalRecord = medicalRecordRepository.save(medicalRecord);
            medicalRecordSyncService.indexMedicalRecord(updatedMedicalRecord);

        } catch (Exception e) {
            throw new PatientException(ErrorCode.FAIL_TO_SAVE_TEST_RECORD);
        }

    }

}
