package org.overcode250204.monitoringservice.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.monitoringservice.grpc.InstrumentSyncGrpcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringSyncEventListener {

    private final ObjectMapper objectMapper;

    private final InstrumentSyncGrpcClient instrumentSyncGrpcClient;

    @KafkaListener(
            topics = "${app.kafka.topics.monitoring.logs:monitoring.logs.publish}",
            groupId = "monitoring-service"
    )
    public void handleTestSyncRequested(Map<String, Object> message) {
        try {
            String eventType = (String) message.get("eventType");
            if (!"TEST_SYNC_REQUESTED".equals(eventType)) return;

            Map<String, Object> payload = (Map<String, Object>) message.get("payload");
            String barcode = (String) payload.get("barcode");
            String requestedBy = (String) payload.get("requestedBy");

            log.info("[Monitoring] Received TEST_SYNC_REQUESTED for barcode={} (requestedBy={})",
                    barcode, requestedBy);

            instrumentSyncGrpcClient.syncUpByBarcode(barcode, requestedBy);

        } catch (Exception e) {
            log.error("[Monitoring] Failed to process TEST_SYNC_REQUESTED event", e);
        }
    }


}
