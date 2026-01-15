package org.overcode250204.monitoringservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.monitoringservice.entities.EventLogs;
import org.overcode250204.monitoringservice.entities.InboxEvent;
import org.overcode250204.monitoringservice.repositories.EventLogsRepo;
import org.overcode250204.monitoringservice.repositories.InboxEventRepository;
import org.overcode250204.monitoringservice.services.MessageTemplateService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogMonitoringListener {

    private final EventLogsRepo eventLogRepository;
    private final MessageTemplateService messageTemplateService;

    private final InboxEventRepository inboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topicPattern = ".*\\.monitoring",
            groupId = "monitoring-service"
    )
    public void onMonitoringEvent(Map<String, Object> message) {
        try {
            UUID eventId = UUID.fromString(message.get("eventId").toString());

            if (inboxEventRepository.existsByEventId(eventId)) {
                log.info("[Inbox] Skip duplicate monitoring log {}", eventId);
                return;
            }

            inboxEventRepository.save(new InboxEvent(
                    UUID.randomUUID().toString(),
                    eventId,
                    objectMapper.writeValueAsString(message),
                    Instant.now()));

            String eventName = (String) message.get("eventType");
            String sourceService = (String) message.get("source");
            Map<String, Object> payload = (Map<String, Object>) message.get("payload");

            String performedBy = "SYSTEM";
            if (payload != null && payload.containsKey("performedBy")) {
                performedBy = (String) payload.get("performedBy");
            }

            String renderedMessage = messageTemplateService.renderMessage(eventName, payload);
            String severity = messageTemplateService.resolveSeverity(eventName);

            EventLogs logEntity = EventLogs.builder()
                    .eventLogId(UUID.randomUUID().toString())
                    .eventName(eventName)
                    .sourceService(sourceService)
                    .payload(payload)
                    .message(renderedMessage)
                    .severity(severity)
                    .performedBy(performedBy)
                    .createdAt(LocalDateTime.now())
                    .build();

            eventLogRepository.save(logEntity);
            log.info("[Monitoring] Logged event={} from {}", eventName, sourceService);
        } catch (Exception e) {
            log.error("[Monitoring] Failed to process event: {}", message, e);
        }
    }
}