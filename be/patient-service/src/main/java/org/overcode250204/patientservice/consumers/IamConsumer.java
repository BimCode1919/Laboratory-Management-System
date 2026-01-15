package org.overcode250204.patientservice.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.patientservice.entities.InboxEvent;
import org.overcode250204.patientservice.entities.Patient;
import org.overcode250204.patientservice.exceptions.ErrorCode;
import org.overcode250204.patientservice.exceptions.PatientException;
import org.overcode250204.patientservice.repositories.InboxRepository;
import org.overcode250204.patientservice.repositories.PatientRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class IamConsumer {
    private final InboxRepository inboxRepository;

    private final PatientRepository patientRepository;

    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "iam.patient.created")
    @Transactional
    public void checkPatientExist(Map<String, Object> payload) {
        try {
            String eventId = (String) payload.get("eventId");
            UUID eventIdUUID = UUID.fromString(eventId);
            if (inboxRepository.existsByEventId(eventIdUUID)) {
                log.info("Event Id: {}  already processed", eventIdUUID);
                return;
            }
            Map<String, Object> data = (Map<String, Object>) payload.get("payload");
            String emailHash = (String) data.get("emailHash");
            Patient patient = patientRepository.findByEmailHash(emailHash);
            if (patient != null) {
                String cognitoSub = (String) data.get("cognitoSub");
                patient.setCognitoSub(cognitoSub);
            }
            InboxEvent inboxEvent = new InboxEvent();
            inboxEvent.setEventId(eventIdUUID);
            inboxEvent.setPayload(objectMapper.writeValueAsString(payload));
            inboxEvent.setProcessedAt(Instant.now());
            inboxRepository.save(inboxEvent);
        } catch (Exception e) {
            throw new PatientException(ErrorCode.FAIL_TO_LISTEN_USER_PATIENT_CREATED);
        }

    }
}
