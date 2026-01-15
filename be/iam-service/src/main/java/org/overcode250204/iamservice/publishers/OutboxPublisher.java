package org.overcode250204.iamservice.publishers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.overcode250204.iamservice.entities.OutboxEvent;
import org.overcode250204.iamservice.repositories.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxEventRepository outboxRepository;

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
            case "IAM_USER_LOGIN",
                 "IAM_DISABLE_USER",
                 "IAM_ENABLE_USER",
                 "IAM_ROLE_DELETED",
                 "IAM_ROLE_UPDATED",
                 "IAM_ROLE_CREATED",
                 "IAM_USER_UPDATED",
                 "IAM_USER_CREATED",
                 "USER_LOGIN_FAILURE",
                 "USER_FIRSTLOGIN_COMPLETED",
                 "USER_CONFIRM_PASSWORD_FAILED",
                 "USER_PASSWORD_FORGOT_FAILED",
                 "USER_PASSWORD_FORGOT_REQUESTED",
                 "USER_PASSWORD_RESET_COMPLETED",
                 "USER_TOKEN_REFRESHED",
                 "USER_TOKEN_REFRESH_FAILED"
                    -> "iam.monitoring";
            case "IAM_USER_PATIENT_CREATED" -> "iam.patient.created";
            default -> null;
        };
    }
}
