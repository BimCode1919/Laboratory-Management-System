package org.overcode250204.warehouseservice.events.listeners;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.overcode250204.warehouseservice.events.InboxEvent;
import org.overcode250204.warehouseservice.model.entities.InstrumentTestExecution;
import org.overcode250204.warehouseservice.model.enums.TestStatus;
import org.overcode250204.warehouseservice.repositories.InboxRepository;
import org.overcode250204.warehouseservice.repositories.InstrumentTestExecutionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestOrderSyncListener {

    private final ObjectMapper objectMapper;
    private final InstrumentTestExecutionRepository executionRepository;
    private final InboxRepository inboxRepository;

    @KafkaListener(
            topics = "${app.kafka.topics.testorder.results.completed:testorder.results.completed}",
            groupId = "warehouse-service-test-results"
    )
    @Transactional
    public void onTestOrderResultsCompleted(ConsumerRecord<String, Object> record) {
        try {
            Map<String, Object> root = (record.value() instanceof String)
                    ? objectMapper.readValue((String) record.value(), new TypeReference<>() {})
                    : objectMapper.convertValue(record.value(), new TypeReference<>() {});

            UUID eventId = UUID.fromString((String) root.get("eventId"));
            UUID testOrderId = UUID.fromString((String) root.get("testOrderId"));
            LocalDateTime completedAt = LocalDateTime.parse((String) root.get("completedAt"));

            // Idempotency
            if (inboxRepository.existsByEventId(eventId)) {
                log.info("Duplicate event {}", eventId);
                return;
            }

            // Load execution
            InstrumentTestExecution exec = executionRepository.findByTestOrderId(testOrderId)
                    .orElse(null);

            if (exec == null) {
                log.warn("No execution found for testOrderId={}", testOrderId);
                return;
            }

            exec.setStatus(TestStatus.PASSED); // or COMPLETED
            exec.setEndTime(completedAt);
            exec.setUpdatedAt(LocalDateTime.now());

            // Optional: summary
            exec.setResultSummary("Execution completed with " +
                    ((List<?>) root.get("results")).size() + " parameters");

            executionRepository.save(exec);

            // inbox
            inboxRepository.save(new InboxEvent(null, eventId,
                    objectMapper.writeValueAsString(root), Instant.now()));

            log.info("Updated execution result for {}", testOrderId);

        } catch (Exception ex) {
            log.error("Error processing completed results event", ex);
        }
    }

}
