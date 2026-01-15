package org.overcode250204.monitoringservice.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.overcode250204.common.grpc.InstrumentSyncServiceGrpc;
import org.overcode250204.common.grpc.RawTestResultMessage;
import org.overcode250204.common.grpc.InstrumentSyncUpRequest;

import org.overcode250204.monitoringservice.entities.OutboxEvent;
import org.overcode250204.monitoringservice.repositories.OutboxEventRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class InstrumentSyncGrpcClient {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    @GrpcClient("instrument-service")
    private InstrumentSyncServiceGrpc.InstrumentSyncServiceBlockingStub syncStub;
    @Value("${app.kafka.topics.monitoring.logs:monitoring.logs.publish}")
    private String monitoringLogsTopic;

    public InstrumentSyncGrpcClient(OutboxEventRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void syncUpByBarcode(String barcode, String requestedBy) {
        InstrumentSyncUpRequest request = InstrumentSyncUpRequest.newBuilder()
                .setBarcode(barcode)
                .setLimit(1)
                .build();

        log.info("[Monitoring] Sending SyncUpRequest for barcode={}", barcode);

        try {
            Iterator<RawTestResultMessage> responses = syncStub.syncUpTestResults(request);
            int count = 0;

            while (responses.hasNext()) {
                RawTestResultMessage msg = responses.next();
                count++;

                // --------------------------------------------------
                // CHỈ MỘT PAYLOAD DUY NHẤT
                // --------------------------------------------------
                Map<String, Object> payload = Map.of(
                        "barcode", msg.getBarcode(),
                        "hl7Message", msg.getHl7Message(),
                        "testType", msg.getTestType(),
                        "status", msg.getStatus(),
                        "runId", msg.getRunId(),
                        "instrumentCode", msg.getInstrumentCode(),
                        "sourceService", "monitoring-service",
                        "syncedBy", requestedBy,
                        "timestamp", LocalDateTime.now().toString()
                );

                Map<String, Object> finalEvent = Map.of(
                        "eventId", UUID.randomUUID().toString(),
                        "eventType", "RAW_RESULT_SYNCED",
                        "payload", payload
                );

                outboxRepository.save(
                        OutboxEvent.builder()
                                .id(UUID.randomUUID().toString())
                                .aggregateType(monitoringLogsTopic)
                                .aggregateId(msg.getBarcode())
                                .eventType("RAW_RESULT_SYNCED")
                                .payload(objectMapper.writeValueAsString(finalEvent))
                                .status("PENDING")
                                .createdAt(LocalDateTime.now())
                                .build()
                );
            }

            // NOT FOUND
            if (count == 0) {
                Map<String, Object> payload = Map.of(
                        "barcode", barcode,
                        "status", "NOT_FOUND",
                        "reason", "No raw results returned by instrument",
                        "sourceService", "monitoring-service",
                        "syncedBy", requestedBy,
                        "timestamp", LocalDateTime.now().toString()
                );

                Map<String, Object> finalEvent = Map.of(
                        "eventId", UUID.randomUUID().toString(),
                        "eventType", "RAW_RESULT_NOT_FOUND",
                        "payload", payload
                );

                outboxRepository.save(
                        OutboxEvent.builder()
                                .id(UUID.randomUUID().toString())
                                .aggregateType(monitoringLogsTopic)
                                .aggregateId(barcode)
                                .eventType("RAW_RESULT_NOT_FOUND")
                                .payload(objectMapper.writeValueAsString(finalEvent))
                                .status("PENDING")
                                .createdAt(LocalDateTime.now())
                                .build()
                );
            }

        } catch (Exception e) {
            log.error("[Monitoring] gRPC SyncUp FAILED for barcode={}", barcode, e);

            Map<String, Object> payload = Map.of(
                    "barcode", barcode,
                    "error", e.getMessage(),
                    "sourceService", "monitoring-service",
                    "timestamp", LocalDateTime.now().toString()
            );

            Map<String, Object> finalEvent = Map.of(
                    "eventId", UUID.randomUUID().toString(),
                    "eventType", "RAW_RESULT_SYNC_FAILED",
                    "payload", payload
            );

            try {
                outboxRepository.save(
                        OutboxEvent.builder()
                                .id(UUID.randomUUID().toString())
                                .aggregateType(monitoringLogsTopic)
                                .aggregateId(barcode)
                                .eventType("RAW_RESULT_SYNC_FAILED")
                                .payload(objectMapper.writeValueAsString(finalEvent))
                                .status("PENDING")
                                .createdAt(LocalDateTime.now())
                                .build()
                );
            } catch (JsonProcessingException ee) {
                log.error("[Monitoring] JSON serialization failed for RAW_RESULT_SYNC_FAILED", ee);
            }
        }
    }
}
