package org.overcode250204.instrumentservice.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class HL7Publisher {


    public Map<String, Object> publishRawBackup(String instrumentCode, UUID runId, String barcode, String hl7Message) {
        Map<String, Object> data = Map.of(
                "instrumentCode", instrumentCode,
                "runId", runId.toString(),
                "barcode", barcode,
                "hl7Message", hl7Message,
                "timestamp", LocalDateTime.now().toString()
        );

        return Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", "HL7_RAW_BACKUP",
                "source", "instrument-service",
                "payload", data,
                "createdAt", LocalDateTime.now().toString()
        );
    }

    public Map<String, Object> publishTestResult(String instrumentCode, String barcode,
                                                    String hl7Message, UUID runId,
                                                    UUID userId, String reagentSnapshot,
                                                    String instrument) {
        Map<String, Object> data = Map.of(
                "instrumentCode", instrumentCode,
                "barcode", barcode,
                "hl7Message", hl7Message,
                "runId", runId.toString(),
                "performedBy", userId != null ? userId.toString() : "SYSTEM",
                "timestamp", LocalDateTime.now().toString(),
                "reagentSnapshot", reagentSnapshot,
                "instrumentDetails", instrument
        );

        return Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", "HL7_TEST_RESULT",
                "source", "instrument-service",
                "payload", data,
                "createdAt", LocalDateTime.now().toString()
        );

    }

}
