package org.overcode250204.patientservice.services.imps;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.patientservice.dtos.PatientDTO;
import org.overcode250204.patientservice.entities.MedicalRecord;
import org.overcode250204.patientservice.entities.OutboxEvent;
import org.overcode250204.patientservice.entities.Patient;
import org.overcode250204.patientservice.exceptions.ErrorCode;
import org.overcode250204.patientservice.exceptions.PatientException;
import org.overcode250204.patientservice.repositories.MedicalRecordRepository;
import org.overcode250204.patientservice.repositories.OutboxRepository;
import org.overcode250204.patientservice.repositories.PatientRepository;
import org.overcode250204.patientservice.services.MedicalRecordSyncService;
import org.overcode250204.patientservice.services.PatientService;
import org.overcode250204.patientservice.utils.AuditLogUtils;
import org.overcode250204.patientservice.utils.MaskData;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImp implements PatientService {

    private final PatientRepository patientRepository;

    private final MedicalRecordRepository medicalRecordRepository;

    private final ObjectMapper objectMapper;

    private final OutboxRepository outboxRepository;

    private final AuditLogUtils auditLogUtils;

    private final MedicalRecordSyncService medicalRecordSyncService;

    @Override
    public PatientDTO updatePatient(UUID patientCode, PatientDTO patientDTO, String updatedBy) {

        Patient savedPatient = null;
        MedicalRecord medicalRecord = null;
        try {
            Patient patient = patientRepository.findByPatientCode(patientCode)
                    .orElseThrow(() -> new PatientException(ErrorCode.PATIENT_CODE_DOES_NOT_EXIST));
            if (patientRepository.existsByEmail(patientDTO.getEmail())) {
                throw new PatientException(ErrorCode.EMAIL_IS_USED);
            }

            if (patientRepository.existsByPhone(patientDTO.getPhoneNumber())) {
                throw new PatientException(ErrorCode.PHONE_IS_USED);
            }
            List<String> changes = new ArrayList<>();

            auditLogUtils.updateField(patient.getAddress(), patientDTO.getAddress(), patient::setAddress, "address", changes);
            auditLogUtils.updateField(patient.getFullName(), patientDTO.getFullName(), patient::setFullName, "fullName", changes);
            auditLogUtils.updateField(patient.getAddress(), patientDTO.getAddress(), patient::setAddress, "address", changes);
            auditLogUtils.updateField(patient.getGender(), patientDTO.getGender(), patient::setGender, "gender", changes);
            if (patientDTO.getEmail() != null) {
                patient.setEmail(patientDTO.getEmail());
                changes.add(String.format("Field 'email' was updated (from: '%s' to: '%s')",
                        MaskData.maskData(patient.getEmail(), true), MaskData.maskData(patientDTO.getEmail(), true)));
            }
            if (patientDTO.getPhoneNumber() != null) {
                patient.setPhone(patientDTO.getPhoneNumber());
                changes.add(String.format("Field 'phone' was updated (from: '%s' to: '%s')",
                        MaskData.maskData(patient.getPhone(), true), MaskData.maskData(patientDTO.getPhoneNumber(), true)));
            }

            if (patientDTO.getDateOfBirth() != null) {
                patient.setDateOfBirth(patientDTO.getDateOfBirth());
                int newAge = Period.between(patientDTO.getDateOfBirth(), LocalDate.now()).getYears();
                patient.setAge(newAge);
                changes.add(String.format("Field 'dateOfBirth' was updated (to: '%s')", patientDTO.getDateOfBirth()));
                changes.add(String.format("Field 'age' was updated (to: '%d')", newAge));
            }
            patient.setUpdatedBy(UUID.fromString(updatedBy));
            patient.setUpdatedAt(Instant.now());
            savedPatient = patientRepository.save(patient);



            Map<String, Object> payload = new HashMap<>();
            payload.put("address", savedPatient.getAddress());
            payload.put("email", savedPatient.getEmail());
            payload.put("phone", savedPatient.getPhone());
            payload.put("dateOfBirth", savedPatient.getDateOfBirth());
            payload.put("fullName", savedPatient.getFullName());
            payload.put("gender", savedPatient.getGender());
            payload.put("patientCode", savedPatient.getPatientCode());
            payload.put("updatedBy", savedPatient.getUpdatedBy());
            payload.put("updatedAt", savedPatient.getUpdatedAt());

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateType("PATIENT_UPDATED");
            outboxEvent.setAggregateId(patient.getId().toString());
            outboxEvent.setEventType("PATIENT_PATIENT_UPDATED");
            outboxEvent.setPayload(objectMapper.writeValueAsString(payload));
            outboxEvent.setCreatedAt(Instant.now());
            outboxRepository.save(outboxEvent);

            if (!changes.isEmpty()) {
                medicalRecord = medicalRecordRepository.findByPatientId(patient.getId())
                        .orElseThrow(() -> new PatientException(ErrorCode.PATIENT_DOES_NOT_EXIST));
                medicalRecordSyncService.indexMedicalRecord(medicalRecord);
                createOutboxEvent(patient, "PATIENT_PATIENT_UPDATED_MONITORING", changes, updatedBy);
            }
        } catch (Exception e) {
            log.warn("Fail to updatePatient: {}", e.getMessage());
            auditLogUtils.createAuditOutboxEvent(
                    "PATIENT_UPDATE_FAILED",
                    patientCode.toString(),
                    "PATIENT_PATIENT_UPDATED",
                    updatedBy,
                    Map.of("exception", e.getMessage())
            );
        }


        return mapToPatientDTO(savedPatient);
    }

    private PatientDTO mapToPatientDTO(Patient patient) {
        PatientDTO patientDTO = null;
        if (patient != null) {
            patientDTO = new PatientDTO();
            patientDTO.setAddress(patient.getAddress());
            patientDTO.setEmail(patient.getEmail());
            patientDTO.setPhoneNumber(patient.getPhone());
            patientDTO.setDateOfBirth(patient.getDateOfBirth());
            patientDTO.setFullName(patient.getFullName());
            patientDTO.setGender(patient.getGender());
            patientDTO.setPatientCode(patient.getPatientCode().toString());
        }
        return patientDTO;
    }

    private void createOutboxEvent(Patient patient, String eventType, List<String> changes, String userIdAction) {
        try {
            Map<String, Object> payload = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "aggregateId", patient.getId().toString(),
                    "patientId", patient.getId().toString(),
                    "userIdAction", userIdAction,
                    "changes", changes
            );

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setEventType(eventType);
            outboxEvent.setPayload(objectMapper.writeValueAsString(payload));
            outboxEvent.setAggregateId(patient.getId().toString());
            outboxEvent.setCreatedAt(Instant.now());
            outboxRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new PatientException(ErrorCode.FAIL_TO_CREATE_OUTBOX_EVENT_TO_SEND_MONITORING_SERVICE);
        }
    }
}
