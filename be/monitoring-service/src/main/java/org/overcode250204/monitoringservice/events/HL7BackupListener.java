package org.overcode250204.monitoringservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.monitoringservice.entities.HL7Backup;
import org.overcode250204.monitoringservice.entities.InboxEvent;
import org.overcode250204.monitoringservice.entities.OutboxEvent;
import org.overcode250204.monitoringservice.enums.RawTestResultStatus;
import org.overcode250204.monitoringservice.repositories.HL7BackupRepo;
import org.overcode250204.monitoringservice.repositories.InboxEventRepository;
import org.overcode250204.monitoringservice.repositories.OutboxEventRepository;
import org.overcode250204.monitoringservice.services.S3BackupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class HL7BackupListener {

    private final HL7BackupRepo backupRepository;

    private final S3BackupService s3BackupService;

    @Value("${app.kafka.topics.monitoring.events:monitoring.events}")
    private String monitoringEventsTopic;


    private final InboxEventRepository inboxEventRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.hl7.rawBackup}", groupId = "monitoring-service")
    public void onHL7BackupMessage(Map<String, Object> payload) {
        try {

            UUID eventId = UUID.fromString(payload.get("eventId").toString());

            // 2) Check duplicate (Inbox Pattern)
            if (inboxEventRepository.existsByEventId(eventId)) {
                log.info("[Inbox] Skip duplicate HL7_RAW_BACKUP {}", eventId);
                return;
            }

            inboxEventRepository.save(new InboxEvent(
                    UUID.randomUUID().toString(),
                    eventId,
                    objectMapper.writeValueAsString(payload),
                    Instant.now()));

            Map<String, Object> data = (Map<String, Object>) payload.get("payload");

            String runId = (String) data.get("runId");
            String barcode = (String) data.get("barcode");
            String instrumentCode = (String) data.get("instrumentCode");
            String hl7Message = (String) data.get("hl7Message");


            // Upload lên S3
            String s3Key = s3BackupService.uploadHL7(barcode, hl7Message);

            // Lưu vào Mongo
            HL7Backup entity = HL7Backup.builder()
                    .backupId(UUID.randomUUID().toString())
                    .runId(runId)
                    .barcode(barcode)
                    .instrumentId(instrumentCode)
                    .hl7Message(hl7Message)
                    .s3Key(s3Key)
                    .status(RawTestResultStatus.RECEIVED)
                    .createdAt(LocalDateTime.now())
                    .build();

            backupRepository.save(entity);
            log.info("[HL7BackupListener]  Backup success for barcode={} runId={}", barcode, runId);

            // Gửi xác nhận lại Kafka topic khác
            Map<String, Object> confirmPayload  = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", "HL7_BACKUP_CONFIRMED",
                    "payload", Map.of(
                            "runId", runId,
                            "barcode", barcode,
                            "status", "SUCCESS"
                    ),
                    "timestamp", LocalDateTime.now().toString()
            );
            outboxRepository.save(
                    OutboxEvent.builder()
                            .id(UUID.randomUUID().toString())
                            .aggregateType(monitoringEventsTopic)
                            .aggregateId(barcode)
                            .eventType("HL7_BACKUP_CONFIRMED")
                            .payload(objectMapper.writeValueAsString(confirmPayload))
                            .status("PENDING")
                            .build()
            );
            log.info("[HL7Backup] Published confirmation for barcode={} runId={}", barcode, runId);



        } catch (Exception e) {
            log.error("[HL7BackupListener]  Error while processing HL7 backup message", e);
        }
    }
}
