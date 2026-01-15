package org.overcode250204.testorderservice.grpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.events.MonitoringPublisher;
import org.overcode250204.testorderservice.exceptions.ErrorCode;
import org.overcode250204.testorderservice.exceptions.TestOrderException;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.repositories.TestOrdersRepository;
import org.overcode250204.testorderservice.utils.AuthUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncUpTestOrderCommandHandler {

    private final MonitoringSyncGrpcClient monitoringSyncGrpcClient;
    private final TestOrdersRepository testOrdersRepository;
    private final MonitoringPublisher monitoringPublisher;

    public void handle(String barcode) {

        log.info("Requesting sync-up for test order barcode={}", barcode);
        Optional<TestOrders> optionalOrder = testOrdersRepository.findByBarCode(barcode);
        if (optionalOrder.isEmpty()) {
            log.warn("No test order found with barcode={}", barcode);
            throw new TestOrderException(ErrorCode.TEST_NOT_FOUND);
        }
        TestOrders testOrder = optionalOrder.get();

        if (!testOrder.getResults().isEmpty()) {
            log.info("Test order {} already synced, skipping sync-up.", barcode);
            return;
        }
        UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());

        monitoringPublisher.publishMonitoringEvent(
                "TEST_SYNC_REQUESTED",
                barcode,
                Map.of(
                        "barcode", barcode,
                        "requestedBy", userId,
                        "description", "Manual sync-up request sent to Monitoring Service")

        );

        monitoringSyncGrpcClient.requestSyncUp("test-order-service", List.of(barcode));

        log.info("Sync-up command executed successfully for barcode={}", barcode);
    }
}
