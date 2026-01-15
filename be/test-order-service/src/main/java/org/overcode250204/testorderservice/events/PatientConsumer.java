package org.overcode250204.testorderservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.models.entites.InboxEvent;
import org.overcode250204.testorderservice.models.entites.PatientReference;
import org.overcode250204.testorderservice.repositories.InboxRepository;
import org.overcode250204.testorderservice.repositories.PatientReferenceRepository;
import org.overcode250204.testorderservice.services.PatientReferenceService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientConsumer {
    private final InboxRepository inboxRepository;

    private final ObjectMapper objectMapper;

    private final PatientReferenceService patientReferenceService;



    @KafkaListener(topics = "${app.kafka.topics.patient.patientUpdate}", groupId = "${spring.kafka.consumer.group-id}")
    @RetryableTopic(attempts = "5")
    @Transactional
    public void handlePatientEvents(Map<String, Object> message) {
        Map<String, Object> payload;
        UUID eventIdUUID;
        try {
            payload = (Map<String, Object>) message.get("payload");
            if (payload == null) {
                log.warn("[PatientSync] Received message without 'payload'. Skipping.");
                return;
            }
            String eventId = (String) message.get("eventId");
            eventIdUUID = UUID.fromString(eventId);
            if (inboxRepository.existsByEventId(eventIdUUID)) {
                log.info("[PatientSync] Event {} already processed. Skipping.", eventIdUUID);
                return;
            }
            InboxEvent inboxEvent = new InboxEvent();
            inboxEvent.setEventId(eventIdUUID);
            inboxEvent.setProcessedAt(Instant.now());
            inboxEvent.setPayload(objectMapper.writeValueAsString(message));
            inboxRepository.save(inboxEvent);

            patientReferenceService.updatePatientReference(payload);

        } catch (Exception e) {
            log.error("Rolling back. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync Patient event", e);
        }
    }
}
