package org.overcode250204.instrumentservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.overcode250204.instrumentservice.entity.InboxEvent;
import org.overcode250204.instrumentservice.entity.PendingTestOrder;
import org.overcode250204.instrumentservice.enums.Priority;
import org.overcode250204.instrumentservice.enums.Status;
import org.overcode250204.instrumentservice.repository.InboxRepository;
import org.overcode250204.instrumentservice.repository.PendingTestOrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestOrderCreatedListener {

    private final PendingTestOrderRepository repository;
    private final InboxRepository inboxRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.testOrderCreate}",
            groupId = "instrument-service-testorder",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onTestOrderCreated(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> event = record.value();
            log.info("Received TEST_ORDER_CREATED event: {}", event);

            UUID eventId = UUID.fromString((String) event.get("eventId"));

            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Event {} already processed, skipping.", eventId);
                return;
            }

            PendingTestOrder entity = PendingTestOrder.builder()
                    .barCode((String) event.get("barCode"))
                    .testOrderId(UUID.fromString((String) event.get("testOrderId")))
                    .testType((String) event.get("testType"))
                    .priority(Priority.LOW)
                    .patientName((String) event.get("patientName"))
                    .status(Status.PENDING)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            repository.save(entity);
            log.info("Saved PendingTestOrder for barCode={}", entity.getBarCode());

            InboxEvent inboxEvent = new InboxEvent();
            inboxEvent.setEventId(eventId);
            inboxEvent.setPayload(objectMapper.writeValueAsString(event));
            inboxEvent.setProcessedAt(Instant.now());
            inboxRepository.save(inboxEvent);

            log.info("Marked event {} as processed.", eventId);

        } catch (Exception e) {
            log.error("Failed to process TEST_ORDER_CREATED event", e);
        }
    }
}
