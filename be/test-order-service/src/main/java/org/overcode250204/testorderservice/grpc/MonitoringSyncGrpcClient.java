package org.overcode250204.testorderservice.grpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.overcode250204.common.grpc.MonitoringServiceGrpc;
import org.overcode250204.common.grpc.MonitoringSyncUpRequest;
import org.overcode250204.common.grpc.MonitoringSyncUpResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringSyncGrpcClient {

    @GrpcClient("monitoring-service")
    private MonitoringServiceGrpc.MonitoringServiceBlockingStub blockingStub;

    public void requestSyncUp(String sourceService, List<String> barcodes) {
        MonitoringSyncUpRequest request = MonitoringSyncUpRequest.newBuilder()
                .setSourceService(sourceService)
                .addAllBarcodes(barcodes)
                .build();

        log.info("Sending Sync-Up request to Monitoring: {}", barcodes);

        MonitoringSyncUpResponse response = blockingStub.requestSyncUp(request);
        log.info("Monitoring acknowledged Sync-Up: {}", response.getMessage());
    }
}
