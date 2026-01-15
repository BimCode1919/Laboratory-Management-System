package org.overcode250204.patientservice.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.patientservice.entities.ClinicalNote;
import org.overcode250204.patientservice.entities.InboxEvent;
import org.overcode250204.patientservice.entities.MedicalRecord;
import org.overcode250204.patientservice.exceptions.ErrorCode;
import org.overcode250204.patientservice.exceptions.PatientException;
import org.overcode250204.patientservice.repositories.*;
import org.overcode250204.patientservice.services.MedicalRecordSyncService;
import org.overcode250204.patientservice.services.TestRecordService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestOrderConsumer {

    private final InboxRepository inboxRepository;

    private final PatientRepository patientRepository;

    private final MedicalRecordRepository medicalRecordRepository;

    private final ObjectMapper objectMapper;

    private final TestRecordService testRecordService;

    private final MedicalRecordSyncService medicalRecordSyncService;

    @KafkaListener(topics = "${app.kafka.topics.listener.test-order.testorderCreated}", groupId = "${spring.kafka.consumer.group-id}")
    @RetryableTopic(attempts = "5")
    @Transactional
    public void testOrderCreated(Map<String, Object> payload) {

            try {
                String testOrderId = (String) payload.get("testOrderId");
                String eventId = (String) payload.get("eventId");
                UUID eventIdUUID = UUID.fromString(eventId);
                if (inboxRepository.existsByEventId(eventIdUUID)) {
                    log.info("Test Order already prcessed for testOrderId={} eventId = {}", testOrderId, eventId);
                    return;
                }

                UUID medicalRecordId = UUID.fromString((String) payload.get("medicalRecordId"));

                MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                        .orElseThrow(() -> new IllegalStateException("MedicalRecord not found for event: " + eventId));

                medicalRecord.setLastTestDate(Instant.now());
                String clinicalNote =  (String) payload.get("notes");
                log.warn("testOrderCreated: clinicalNote={}", clinicalNote);
                if (clinicalNote != null && clinicalNote.trim().length() > 0) {
                    String noteBy = (String) payload.get("noteBy");
                    ClinicalNote note = new ClinicalNote();
                    note.setCreatedAt(Instant.now());
                    note.setNote(clinicalNote);
                    note.setNotedBy(UUID.fromString(noteBy));
                    note.setMedicalRecord(medicalRecord);
                    medicalRecord.getClinicalNotes().add(note);
                }
                medicalRecordSyncService.indexMedicalRecord(medicalRecord);

                InboxEvent inboxEvent = new InboxEvent();
                inboxEvent.setEventId(eventIdUUID);
                inboxEvent.setPayload(objectMapper.writeValueAsString(payload));
                inboxEvent.setProcessedAt(Instant.now());
                inboxRepository.save(inboxEvent);
            } catch (Exception e) {
                log.error("Error at TestOrderCreatedListener: {}", e.getMessage());
                throw new PatientException(ErrorCode.GET_USER_ID_FROM_IAM_SERVICE_ERROR);
            }
        }


    @KafkaListener(topics = "${app.kafka.topics.listener.test-order.tesrorderCompleted}", groupId = "${spring.kafka.consumer.group-id}")
    @RetryableTopic(attempts = "3")
    @Transactional
    public void testOrderCompleted(Map<String, Object> payload) {
        try {
            String eventId = (String) payload.get("eventId");

            if (inboxRepository.existsByEventId(UUID.fromString(eventId))) {
                log.info("Test Order already prcessed for testOrderId={}", eventId);
                return;
            }

            InboxEvent inboxEvent = new InboxEvent();
            inboxEvent.setEventId(UUID.fromString(eventId));
            inboxEvent.setPayload(objectMapper.writeValueAsString(payload));
            inboxEvent.setProcessedAt(Instant.now());
            inboxRepository.save(inboxEvent);

            testRecordService.saveTestRecord(payload);

        } catch (Exception e) {
            log.error("Error at TestOrderCompletedListener: {}", e.getMessage());
            throw new PatientException(ErrorCode.FAIL_TO_LISTEN_TEST_ORDER_RESULTS_COMPLETED_EVENT);
        }

    }

}
