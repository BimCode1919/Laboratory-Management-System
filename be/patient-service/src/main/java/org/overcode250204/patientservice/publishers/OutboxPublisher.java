package org.overcode250204.patientservice.publishers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.overcode250204.patientservice.entities.OutboxEvent;
import org.overcode250204.patientservice.repositories.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;

    @Value("${spring.application.name}")
    private String serviceName;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxRepository.findByStatus("PENDING");
        for (OutboxEvent event : pending) {
            try {
                Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);
                Map<String, Object> data = Map.of(
                        "eventId", event.getId(),
                        "source", serviceName,
                        "payload", payload,
                        "eventType", event.getEventType(),
                        "timestamp", Instant.now()
                );
                String topic = resolveTopic(event.getEventType());
                if (topic == null) {
                    log.warn("No topic found for eventType: {}", event.getEventType());
                    continue;
                }
                ProducerRecord<String, Object> record =
                        new ProducerRecord<>(topic, event.getAggregateId(), data);
                kafkaTemplate.send(record).whenComplete((result, ex) -> {
                    if (ex == null) {
                        event.setStatus("SENT");
                        outboxRepository.save(event);
                        log.info("Published {} to topic {}", event.getEventType(), topic);
                    } else {
                        log.error("Failed to send event {}: {}", event.getEventType(), ex.getMessage());
                    }
                });
            } catch (Exception e) {
                log.error("Failed to publish pending eventId: {}, Error: {}", event.getId() ,e.getMessage());
            }
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType.toUpperCase()) {
            case "PATIENT_MEDICALRECORD_UPDATED", "PATIENT_MEDICALRECORD_CREATED", "PATIENT_PATIENT_UPDATED_MONITORING" -> "patient.monitoring";
            case "PATIENT_PATIENT_UPDATED", "PATIENT_MEDICALRECORD_UPDATED_TESTORDER" -> "patient.patient.updated";
            default -> null;
        };
    }
}
