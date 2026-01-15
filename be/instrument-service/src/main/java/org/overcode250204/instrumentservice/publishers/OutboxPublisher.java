package org.overcode250204.instrumentservice.publishers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.overcode250204.instrumentservice.entity.OutboxEvent;
import org.overcode250204.instrumentservice.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
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

    // Instrument Topics
    @Value("${app.kafka.topics.configuration.sync:instrument.configuration.sync.request}")
    private String configurationSyncRequestTopic;
    @Value("${app.kafka.topics.configuration.all.sync:instrument.configuration.all.sync.request}")
    private String configurationAllSyncRequestTopic;
    @Value("${app.kafka.topics.instrument.created:instrument.created}")
    private String instrumentCreatedTopic;

    // Reagent Topics
    @Value("${app.kafka.topics.reagent.install:warehouse.reagent.install.request}")
    private String reagentInstallRequestTopic;
    @Value("${app.kafka.topics.reagent.uninstall:warehouse.reagent.uninstall.request}")
    private String reagentUninstallRequestTopic;
    @Value("${app.kafka.topics.reagent.sync.request:warehouse.reagent.sync.request}")
    private String reagentSyncRequestTopic;

    // HL7 and Test Result Topics
    @Value("${app.kafka.topics.analysisRequest}")
    private String analysisRequestTopic;

    @Value("${app.kafka.topics.monitoring}")
    private String hl7RawBackupTopic;

    @Value("${app.kafka.topics.hl7}")
    private String hl7TestResultTopic;

    @Value("${app.kafka.topics.testresult}")
    private String  hl7TestOrderTopic ;

    // Analyzer Run Complete Topics
    @Value("${app.kafka.topics.runCompleted}")
    private String runCompletedTopic;


    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxRepository.findByStatus("PENDING");
        for (OutboxEvent event : pending) {
            try {
                Map<String, Object> eventPayload = objectMapper.readValue(event.getPayload(), new TypeReference<>() {});
                String topic = resolveTopic(event.getEventType());
                if (topic == null) {
                    log.warn("No topic found for eventType: {}", event.getEventType());
                    continue;
                }
                ProducerRecord<String, Object> record =
                        new ProducerRecord<>(topic, event.getAggregateId(), eventPayload);

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
            case "HL7_TEST_RESULT" -> hl7TestOrderTopic;
            // Instrument Event Types
            case "CONFIGURATION_SYNC_REQUEST" -> configurationSyncRequestTopic;
            case "CONFIGURATION_ALL_SYNC_REQUEST" -> configurationAllSyncRequestTopic;
            case "INSTRUMENT_CREATED" -> instrumentCreatedTopic;

            // Reagent Event Types
            case "REAGENT_INSTALL_REQUEST" -> reagentInstallRequestTopic;
            case "REAGENT_UNINSTALL_REQUEST" -> reagentUninstallRequestTopic;
            case "REAGENT_SYNC_REQUEST" -> reagentSyncRequestTopic;

            // HL7 and Test Result Event Types
            case "ANALYSIS_REQUEST" -> analysisRequestTopic;
            case "INSTRUMENT_RUN_COMPLETION_LOG",
                 "INSTRUMENT_RUN_STARTED",
                 "INSTRUMENT_RAW_DELETED",
                 "RAW_RESULT_AUTO_CLEANUP",
                 "INSTRUMENT_CONFIGURATION_SYNC_REQUEST_LOG",
                 "INSTRUMENT_CONFIGURATION_ALL_SYNC_REQUEST_LOG",
                 "REAGENT_INSTALL_REQUEST_LOG",
                 "REAGENT_SYNC_REQUEST_LOG",
                 "REAGENT_UNINSTALL_REQUEST_LOG",
                 "INSTRUMENT_MODE_CHANGED"  -> hl7RawBackupTopic;



            case "HL7_RAW_BACKUP" -> hl7TestResultTopic;


            case "INSTRUMENT_RUN_COMPLETED" -> runCompletedTopic;

            default -> null;
        };
    }
}
