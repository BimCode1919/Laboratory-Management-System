package org.overcode250204.patientservice.services.imps;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.patientservice.dtos.*;
import org.overcode250204.patientservice.entities.*;
import org.overcode250204.patientservice.enums.TestRecordStatus;
import org.overcode250204.patientservice.exceptions.ErrorCode;
import org.overcode250204.patientservice.exceptions.PatientException;
import org.overcode250204.patientservice.repositories.*;
import org.overcode250204.patientservice.services.MedicalRecordService;
import org.overcode250204.patientservice.services.MedicalRecordSyncService;
import org.overcode250204.patientservice.services.PatientService;
import org.overcode250204.patientservice.utils.AuditLogUtils;
import org.overcode250204.patientservice.utils.HashUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;


    private final ObjectMapper objectMapper;

    private final MedicalRecordHistoryRepository medicalRecordHistoryRepository;

    private final PatientRepository patientRepository;

    private final TestRecordRepository testRecordRepository;

    private final OutboxRepository outboxRepository;

    private final AuditLogUtils auditLogUtils;

    private final MedicalRecordSyncService medicalRecordSyncService;

    private final MedicalRecordSearchRepository searchRepository;


    private final HashUtil hashUtil;

    private MedicalRecordDTO toResponseDTO(MedicalRecord record) {
        return MedicalRecordDTO.builder()
                .recordId(record.getId())
                .patient(record.getPatient())
                .fullName(record.getPatient().getFullName())
                .dateOfBirth(record.getPatient().getDateOfBirth())
                .gender(record.getPatient().getGender() != null ? record.getPatient().getGender().name()  : null)
                .createdAt(record.getCreatedAt())
                .createdBy(record.getCreatedBy())
                .updatedAt(record.getUpdatedAt())
                .updatedBy(record.getUpdatedBy())
                .totalNotes(record.getClinicalNotes() != null ? record.getClinicalNotes().size() : 0)
                .clinicalNotes(record.getClinicalNotes())
                .testRecords(record.getTestRecords() != null ? record.getTestRecords() : null)
                .build();
    }

    @Override
    @Transactional
    public MedicalRecordDTO addRecord(MedicalRecordCreateDTO dto, String createdByLabUser) {
        MedicalRecord savedRecord = null;
        Patient savedPatient = null;
        try {
            String emailHash = hashUtil.hmacSha256Base64(dto.getEmail());
            String phoneHash = hashUtil.hmacSha256Base64(dto.getPhoneNumber());
            Optional<Patient> patientExisted = patientRepository.findByDateOfBirthAndPhoneOrEmail(dto.getDateOfBirth(), phoneHash, emailHash);
            if (!patientExisted.isEmpty()) {
                throw new PatientException(ErrorCode.PATIENT_EXISTED);
            }
            Patient patient = new Patient();
            patient.setFullName(dto.getFullName());
            patient.setDateOfBirth(dto.getDateOfBirth());
            patient.setGender(dto.getGender());
            patient.setPhone(dto.getPhoneNumber());
            patient.setAddress(dto.getAddress());
            patient.setEmail(dto.getEmail());
            patient.setEmailHash(emailHash);
            patient.setPhoneHash(phoneHash);
            if (dto.getDateOfBirth() != null) {
                patient.setAge(Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears());
            }
            patient.setCreatedAt(Instant.now());
            patient.setPatientCode(UUID.randomUUID());
            savedPatient = patientRepository.save(patient);

            MedicalRecord record = new MedicalRecord();
            record.setPatient(savedPatient);
            record.setCreatedBy(UUID.fromString(createdByLabUser));
            record.setVisitDate(Instant.now());
            savedRecord = medicalRecordRepository.save(record);

            List<String> changes = List.of(String.format("New MedicalRecord (ID: %s) created for Patient (ID: %s)",
                    savedRecord.getId(), savedPatient.getId()));
            createOutboxEvent(savedRecord, "PATIENT_MEDICALRECORD_CREATED", changes, createdByLabUser);
        } catch (Exception e) {
            throw new PatientException(ErrorCode.ERROR_TO_CREATE_MEDICAL_RECORD);
        }

        medicalRecordSyncService.indexMedicalRecord(savedRecord);

        return toResponseDTO(savedRecord);
    }

    @Override
    @Transactional
    public MedicalRecordDTO updateRecord(UUID recordId, MedicalRecordUpdateDTO medicalRecordUpdateDTO, String updatedBy) {
        MedicalRecord medicalRecord = null;
        try {
            if (medicalRecordUpdateDTO.getPatientDTO() != null) {
                PatientDTO patientDTO = medicalRecordUpdateDTO.getPatientDTO();

                if (patientDTO.getEmail() != null && patientRepository.existsByEmail(patientDTO.getEmail())) {
                    throw new PatientException(ErrorCode.EMAIL_ALREADY_USED);
                }
                if (patientDTO.getPhoneNumber() != null && patientRepository.existsByPhone(patientDTO.getPhoneNumber())) {
                    throw new PatientException(ErrorCode.PHONE_ALREADY_USED);
                }
            }

            //Find medical record by Id
            medicalRecord = medicalRecordRepository.findById(recordId)
                    .orElseThrow(() -> new PatientException(ErrorCode.RECORD_NOT_FOUND));

            //If patientCode of medical record doesn't matching with patientCode at client request that will throw ex
            Patient patient = medicalRecord.getPatient();
            createHistoryVersion(patient, medicalRecord, updatedBy);


            List<String> changes = new ArrayList<>();

            if (medicalRecordUpdateDTO.getPatientDTO() != null) {
                PatientDTO patientDTO = medicalRecordUpdateDTO.getPatientDTO();
                auditLogUtils.updateField(patient.getPhone(), patientDTO.getPhoneNumber(), patient::setPhone, "phone", changes);
                auditLogUtils.updateField(patient.getEmail(), patientDTO.getEmail(), patient::setEmail, "email", changes);
                auditLogUtils.updateField(patient.getAddress(), patientDTO.getAddress(), patient::setAddress, "address", changes);
                auditLogUtils.updateField(patient.getFullName(), patientDTO.getFullName(), patient::setFullName, "fullName", changes);

                patient.setUpdatedBy(UUID.fromString(updatedBy));
                patient.setUpdatedAt(Instant.now());

            }

            if (medicalRecordUpdateDTO.getClinicalNotes() != null && !medicalRecordUpdateDTO.getClinicalNotes().isEmpty()) {
                for (String noteText : medicalRecordUpdateDTO.getClinicalNotes()) {
                    ClinicalNote newNote = new ClinicalNote();
                    newNote.setNote(noteText);
                    newNote.setNotedBy(UUID.fromString(updatedBy));
                    newNote.setMedicalRecord(medicalRecord);
                    newNote.setCreatedAt(Instant.now());
                    medicalRecord.getClinicalNotes().add(newNote);
                }
                changes.add("Added: " + medicalRecordUpdateDTO.getClinicalNotes().size() + " new clinical notes");
            }
            if (medicalRecordUpdateDTO.getInterpretation() != null && !medicalRecordUpdateDTO.getInterpretation().isEmpty()) {

                // (Tối ưu: Load 1 lần để tránh N+1 query)
                Map<UUID, TestRecord> testRecordMap = medicalRecord.getTestRecords().stream()
                        .collect(Collectors.toMap(TestRecord::getTestOrderId, Function.identity()));

                //testRecordId FE need to fill testOrderId to lab_user can review a few testOrderId on one patient's medical record
                for (InterpretationUpdateDTO interpretationDTO : medicalRecordUpdateDTO.getInterpretation()) {
                    TestRecord testRecord = testRecordMap.get(interpretationDTO.getTestRecordId());

                    if (testRecord != null) {
                        auditLogUtils.updateField(testRecord.getInterpretation(), interpretationDTO.getInterpretation(),
                                testRecord::setInterpretation, "interpretation (TestRecordId: " + testRecord.getId() + ")", changes);
                        TestRecordStatus newStatus = (interpretationDTO.getStatus() != null)
                                ? interpretationDTO.getStatus()
                                : TestRecordStatus.REVIEWED;
                        auditLogUtils.updateField(testRecord.getStatus(), newStatus,
                                testRecord::setStatus, "status (TestRecordId: " + testRecord.getId() + ")", changes);
                        testRecord.setReviewedBy(UUID.fromString(updatedBy));
                        testRecord.setReviewedAt(Instant.now());
                        testRecordRepository.save(testRecord);
                    }
                }
            }
            if (!changes.isEmpty()) {
                medicalRecordRepository.save(medicalRecord);
                medicalRecordSyncService.indexMedicalRecord(medicalRecord);
                createOutboxEvent(medicalRecord, "PATIENT_MEDICALRECORD_UPDATED", changes, updatedBy);
                OutboxEvent outboxEvent = new OutboxEvent();
                outboxEvent.setEventType("PATIENT_MEDICALRECORD_UPDATED_TESTORDER");
                outboxEvent.setPayload(objectMapper.writeValueAsString(medicalRecord.getPatient()));
                outboxEvent.setAggregateId(medicalRecord.getId().toString());
                outboxEvent.setAggregateType("PATIENT_MEDICALRECORD_UPDATED_TESTORDER");
                outboxRepository.save(outboxEvent);
            }
        } catch (Exception e) {
            throw new PatientException(ErrorCode.FAIL_TO_UPDATE_MEDICALRECORD);
        }



        return toResponseDTO(medicalRecord);
    }


    private void createHistoryVersion(Patient patient, MedicalRecord medicalRecord, String changedBy) {
        try {
            // 1. Xử lý danh sách TestRecords
            List<Map<String, Object>> safeTestRecords = medicalRecord.getTestRecords().stream()
                    .map(t -> {
                        Map<String, Object> testRecordMap = new HashMap<>();
                        testRecordMap.put("id", t.getId());
                        testRecordMap.put("testOrderId", t.getTestOrderId());
                        testRecordMap.put("status", t.getStatus() != null ? t.getStatus().name() : null);
                        testRecordMap.put("interpretation", t.getInterpretation());
                        return testRecordMap;
                    })
                    .toList();

            // 2. Xử lý Clinical Notes
            List<String> clinicalNotesList = medicalRecord.getClinicalNotes() != null
                    ? medicalRecord.getClinicalNotes().stream().map(ClinicalNote::getNote).toList()
                    : new ArrayList<>();

            Map<String, Object> patientMap = new HashMap<>();
            patientMap.put("id", patient.getId());
            patientMap.put("fullName", patient.getFullName());
            patientMap.put("phone", patient.getPhone());
            patientMap.put("email", patient.getEmail());
            patientMap.put("address", patient.getAddress());

            // 4. Xử lý thông tin MedicalRecord (Dùng HashMap)
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", medicalRecord.getId());
            recordMap.put("createdAt", medicalRecord.getCreatedAt());
            recordMap.put("visitDate", medicalRecord.getVisitDate());
            recordMap.put("testRecords", safeTestRecords);

            // 5. Tổng hợp snapshot
            Map<String, Object> snapShotData = new HashMap<>();
            snapShotData.put("patient", patientMap);
            snapShotData.put("medicalRecord", recordMap);
            snapShotData.put("clinicalNotes", clinicalNotesList);

            String backupDataJson = objectMapper.writeValueAsString(snapShotData);

            MedicalRecordHistory history = new MedicalRecordHistory();
            history.setPatient(patient);
            history.setBackupData(backupDataJson);
            history.setChangedBy(changedBy);
            history.setChangedAt(Instant.now());

            long currentVersion = medicalRecordHistoryRepository.countByPatient_PatientCode(patient.getPatientCode());
            history.setVersion(currentVersion + 1);

            medicalRecordHistoryRepository.save(history);

        } catch (Exception e) {
            log.error("Error at MedicalRecord with method createHistoryVersion: {}", e.getMessage(), e);
            // Log rõ stack trace để debug nếu vẫn còn lỗi
            e.printStackTrace();
            throw new PatientException(ErrorCode.BACKUP_MEDICAL_RECORD_DATA_FAIL);
        }
    }

    @Override
    @Transactional
    public String deleteRecord(UUID recordId, String deletedByLabUser) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new PatientException(ErrorCode.RECORD_NOT_FOUND));

        boolean hasPendingTests = record.getTestRecords().stream()
                .anyMatch(testRecord ->
                        testRecord.getStatus() == TestRecordStatus.PENDING ||
                                testRecord.getStatus() == null
                );
        if (hasPendingTests) {
            log.warn("UserId {} failed to delete record {}: Record has pending tests.", deletedByLabUser, recordId);
            throw new PatientException(ErrorCode.DELETE_NOT_ALLOWED);
        }

        record.setIsDeleted(true);
        record.setUpdatedBy(UUID.fromString(deletedByLabUser));
        medicalRecordRepository.save(record);

        try {
            searchRepository.deleteById(recordId.toString());
            log.info("Removed document with ID {} from Elasticsearch index.", recordId);
        } catch (Exception e) {
            log.error("Failed to remove document {} from Elasticsearch, but soft delete in DB was successful. Message: {}", recordId, e.getMessage());
        }

        log.info("LOG: Patient Medical Record ID " + recordId + " soft deleted by User ID: " + 1L);
        return "Delete medical record id " + recordId + " successfully ";
    }


    @Override
    public Page<MedicalRecordDTO> getAllRecords(Pageable pageable) {
        Page<MedicalRecord> records = medicalRecordRepository.findAll(pageable);
        return records.map(this::toResponseDTO);
    }

    @Override
    public MedicalRecordDTO getRecordById(UUID recordId, String accessedBy) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new PatientException(ErrorCode.RECORD_NOT_FOUND));

        log.info("LOG: Patient Medical Record ID {} accessed by User ID: {} ", recordId, accessedBy);
        return toResponseDTO(record);
    }

    private void createOutboxEvent(MedicalRecord medicalRecord, String eventType, List<String> changes, String userIdAction) {
        try {
            Map<String, Object> payload = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "aggregateId", medicalRecord.getId().toString(),
                    "patientId", medicalRecord.getPatient().getId().toString(),
                    "userIdAction", userIdAction,
                    "changes", changes
            );

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setEventType(eventType);
            outboxEvent.setPayload(objectMapper.writeValueAsString(payload));
            outboxEvent.setAggregateId(medicalRecord.getId().toString());
            outboxEvent.setCreatedAt(Instant.now());
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new PatientException(ErrorCode.FAIL_TO_CREATE_OUTBOX_EVENT_TO_SEND_MONITORING_SERVICE);
        }




    }




}