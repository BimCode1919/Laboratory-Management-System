package org.overcode250204.testorderservice.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.overcode250204.testorderservice.models.entites.OutboxEvent;
import org.overcode250204.testorderservice.repositories.OutboxRepository;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxRepository.findByStatus("PENDING");
        for (OutboxEvent event : pending) {
            try {
                Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);
                payload.put("eventId", event.getId());
                String topic = resolveTopic(event.getEventType());
                if (topic == null) {
                    log.warn("No binding found for eventType: {}, skipping...", event.getEventType());
                    continue;
                }
                ProducerRecord<String, Object> record = new ProducerRecord<>(topic, event.getAggregateId(), payload);
                kafkaTemplate.send(record).whenComplete((result, exception) -> {
                    if (exception == null) {
                        event.setStatus("SENT");
                        outboxRepository.save(event);
                        log.info("Sent event {} to topic {}", event.getEventType(), topic);
                    } else {
                        log.error("Failed to send event {}: {}", event.getEventType(), exception.getMessage());
                    }
                });

            } catch (Exception e) {
                log.error("Failed to publish pending eventId: {}, Error: {}", event.getId() ,e.getMessage());
            }
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType.toUpperCase()) {
            case "TEST_ORDER_CREATED" -> "testorder.testorder.created";
            case "TEST_ORDER_COMPLETED" -> "testorder.results.completed";
            case "TEST_RAWRESULT_RECEIVED" -> "testorder.rawresult.received";

            case "TEST_RESULTS_PROCESSED",
                 "TEST_COMMENT_ADDED",
                 "TEST_COMMENT_MODIFIED",
                 "TEST_COMMENT_DELETED",
                 "TEST_SYNC_REQUESTED" -> "testorder.monitoring";

            default -> null;
        };
    }


}
