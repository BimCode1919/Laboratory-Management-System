package org.overcode250204.instrumentservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.entity.InboxEvent;
import org.overcode250204.instrumentservice.entity.RawTestResult;
import org.overcode250204.instrumentservice.repository.InboxEventRepository;
import org.overcode250204.instrumentservice.repository.RawTestResultRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringEventListener {

    private final RawTestResultRepository rawTestResultRepository;
    private final InboxEventRepository inboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "monitoring.events", groupId = "instrument-service")
    @Transactional
    public void onMonitoringEvent(Map<String, Object> event) {
        try {
            UUID eventId = UUID.fromString(event.get("eventId").toString());

            if (inboxEventRepository.existsByEventId(eventId)) {
                log.info("[Inbox] Skip duplicate event {}", eventId);
                return;
            }

            String type = (String) event.get("eventType");

            if ("HL7_BACKUP_CONFIRMED".equals(type)) {

                Map<String, Object> payload = (Map<String, Object>) event.get("payload");
                String refRunId = payload.get("runId") != null ? payload.get("runId").toString() : null;
                String barcode = payload.get("barcode") != null ? payload.get("barcode").toString() : null;

                if (refRunId != null && !refRunId.isBlank()) {
                    List<RawTestResult> results = rawTestResultRepository.findByRunId(UUID.fromString(refRunId));
                    results.forEach(r -> r.setBackedUp(true));
                    rawTestResultRepository.saveAll(results);
                    log.info("{} results marked as backedUp for runId={}", results.size(), refRunId);

                } else if (barcode != null && !barcode.isBlank()) {
                    rawTestResultRepository.findByBarcode(barcode).ifPresent(r -> {
                        r.setBackedUp(true);
                        rawTestResultRepository.save(r);
                        log.info("Backup confirmed for barcode={}", barcode);
                    });
                } else {
                    log.warn("HL7_BACKUP_CONFIRMED missing runId and barcode: {}", event);
                }
            } else {
                log.debug("Ignored event type={}", type);
            }

            inboxEventRepository.save(new InboxEvent(
                    null,
                    eventId,
                    objectMapper.writeValueAsString(event),
                    Instant.now()
            ));

        } catch (Exception e) {
            log.error("Failed to process monitoring event: {}", event, e);
        }
    }
}
