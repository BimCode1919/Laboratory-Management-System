package org.overcode250204.monitoringservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.overcode250204.monitoringservice.entities.MessageBrokerHealth; // <-- BỔ SUNG
import org.overcode250204.monitoringservice.entities.OutboxEvent;
import org.overcode250204.monitoringservice.enums.MessageBrokerHealthStatus; // <-- BỔ SUNG
import org.overcode250204.monitoringservice.repositories.MessageBrokerHealthRepo; // <-- BỔ SUNG
import org.overcode250204.monitoringservice.repositories.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional; // <-- BỔ SUNG

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // --- BỔ SUNG ---
    private final MessageBrokerHealthRepo healthRepo;
    private static final String BROKER_NAME = "primary-kafka-cluster";

    @Value("${app.kafka.topics.hl7.rawBackup}")
    private String hl7RawBackupTopic;

    @Value("${app.kafka.topics.monitoring.events}")
    private String monitoringEventsTopic;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {

        // Kiểm tra sức khỏe Broker trước khi gửi
        Optional<MessageBrokerHealth> healthOpt = healthRepo.findByBrokerName(BROKER_NAME);

        // Nếu đã có bản ghi trạng thái, VÀ trạng thái không phải là HEALTHY
        if (healthOpt.isPresent() && healthOpt.get().getStatus() != MessageBrokerHealthStatus.HEALTHY) {
            log.warn("[CircuitBreaker] Broker status is {}, pausing Outbox publisher.",
                    healthOpt.get().getStatus());
            return; // TẠM DỪNG (PAUSE) - không làm gì cả, chờ lần chạy sau
        }

        List<OutboxEvent> pending = outboxRepository.findByStatus("PENDING");
        for (OutboxEvent event : pending) {
            try {
                Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);
                payload.put("eventId", event.getId().toString());

                String topic = resolveTopic(event.getEventType());
                if (topic == null) {
                    log.warn("No topic found for eventType: {}", event.getEventType());
                    continue;
                }
                ProducerRecord<String, Object> record =
                        new ProducerRecord<>(topic, event.getAggregateId(), payload);

                kafkaTemplate.send(record).whenComplete((result, ex) -> {
                    if (ex == null) {
                        event.setStatus("SENT");
                        outboxRepository.save(event);
                        log.info("Published {} to topic {}", event.getEventType(), topic);
                    } else {
                        log.error("Failed to send event {}: {}", event.getEventType(), ex.getMessage());
                        // Không cập nhật status, để scheduler tự động retry ở lần chạy sau
                    }
                });
            } catch (Exception e) {
                log.error("Failed to publish pending eventId: {}, Error: {}", event.getId() ,e.getMessage());
            }
        }
    }

    private String resolveTopic(String eventType) {
        // Bạn sẽ cần bổ sung các eventType mới khi implement SyncUpScheduler
        return switch (eventType.toUpperCase()) {
            case "HL7_BACKUP_CONFIRMED"
                    -> monitoringEventsTopic;

            case "RAW_RESULT_SYNCED",
                 "RAW_RESULT_SYNC_FAILED",
                 "RAW_RESULT_NOT_FOUND" -> "monitoring.logs.publish";
            default -> null;
        };
    }
}