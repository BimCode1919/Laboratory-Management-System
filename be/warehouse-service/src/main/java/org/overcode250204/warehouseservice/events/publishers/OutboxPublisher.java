package org.overcode250204.warehouseservice.events.publishers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.overcode250204.warehouseservice.events.OutboxEvent;
import org.overcode250204.warehouseservice.repositories.OutboxRepository;
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
    @Value("${app.kafka.topics.configuration.sync.response:instrument.configuration.sync.response}")
    private String configurationSyncResponseTopic;
    @Value("${app.kafka.topics.configuration.all.sync.response:instrument.configuration.all.sync.response}")
    private String configurationAllSyncResponseTopic;

    // Reagent Topics
    @Value("${app.kafka.topics.reagent.install.response:warehouse.reagent.install.response}")
    private String reagentInstallResponseTopic;
    @Value("${app.kafka.topics.reagent.sync.response:warehouse.reagent.sync.response}")
    private String reagentSyncResponseTopic;

    // Monitoring Topic
    @Value("${app.kafka.topics.monitoring:warehouse.monitoring}")
    private String warehouseMonitoringTopic;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus("PENDING");
        for (OutboxEvent event : pendingEvents) {
            try {
                Map<String, Object> payload = objectMapper.readValue(event.getPayload(), new TypeReference<>() {
                });
                String topic = resolveTopic(event.getEventType());

                if (topic == null) {
                    log.warn("No topic found for eventType: {}", event.getEventType());
                    continue;
                }

                ProducerRecord<String, Object> record = new ProducerRecord<>(topic, event.getAggregateId(), payload);

                kafkaTemplate.send(record).whenComplete((result, ex) -> {
                    if (ex == null) {
                        event.setStatus("SENT");
                        outboxRepository.save(event);
                        log.info("Published event {} to topic {}", event.getEventType(), topic);
                    } else {
                        log.error("Failed to send event {}: {}", event.getEventType(), ex.getMessage());
                    }
                });
            } catch (Exception e) {
                log.error("Failed to publish pending eventId: {}, Error: {}", event.getId(), e.getMessage());
            }
        }
    }

    private String resolveTopic(String eventType) {
        return switch (eventType.toUpperCase()) {
            // Instrument Event Types
            case "CONFIGURATION_SYNC_RESPONSE" -> configurationSyncResponseTopic;
            case "CONFIGURATION_ALL_SYNC_RESPONSE" -> configurationAllSyncResponseTopic;

            // Reagent Event Types
            case "REAGENT_INSTALL_RESPONSE" -> reagentInstallResponseTopic;
            case "REAGENT_SYNC_RESPONSE" -> reagentSyncResponseTopic;

            // Monitoring Event Types
            case "CONFIGURATION_CREATED",
                 "CONFIGURATION_UPDATED",
                 "CONFIGURATION_DELETED",
                 "INSTRUMENT_CREATED",
                 "INSTRUMENT_DELETED",
                 "INSTRUMENT_UPDATED",
                 "INSTRUMENT_ACTIVE",
                 "INSTRUMENT_INACTIVE",
                 "INSTRUMENT_AUTO_DELETED",
                 "REAGENT_SUPPLY_ADDED",
                 "REAGENT_CREATE",
                 "REAGENT_UPDATE",
                 "REAGENT_DELETED"
//                 "REAGENT_USAGE_LOGGED"
                    -> warehouseMonitoringTopic;

            default -> null;
        };
    }
}