package org.overcode250204.monitoringservice.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.overcode250204.common.grpc.MonitoringServiceGrpc;
import org.overcode250204.monitoringservice.dtos.SyncUpRequestsDTO;
import org.overcode250204.monitoringservice.enums.SyncUpRequestsStatus;
import org.overcode250204.monitoringservice.services.SyncUpRequestsService;
import org.overcode250204.common.grpc.MonitoringSyncUpRequest;
import org.overcode250204.common.grpc.MonitoringSyncUpResponse;


@GrpcService
@Slf4j
@RequiredArgsConstructor
public class MonitoringGrpcServiceImpl extends MonitoringServiceGrpc.MonitoringServiceImplBase {

    private final SyncUpRequestsService syncUpRequestsService;
    private final ObjectMapper objectMapper; // Dùng để chuyển list barcodes -> JSON string

    @Override
    public void requestSyncUp(MonitoringSyncUpRequest request,
                              StreamObserver<MonitoringSyncUpResponse> responseObserver) {
        log.info("Received gRPC Sync-Up request from service: {}, for {} barcodes",
                request.getSourceService(), request.getBarcodesCount());

        try {
            // Chuyển List<String> barcodes thành một chuỗi JSON để lưu vào CSDL
            String messagePayload = objectMapper.writeValueAsString(request.getBarcodesList());

            // Tạo DTO và gọi Service
            SyncUpRequestsDTO dto = SyncUpRequestsDTO.builder()
                    .sourceService(request.getSourceService())
                    .messageId(messagePayload) // Lưu payload (list barcodes) vào messageId
                    .status(SyncUpRequestsStatus.PENDING.name()) // Đặt là PENDING
                    .build();

            syncUpRequestsService.create(dto); // Lưu "Job" này lại

            // Phản hồi thành công
            MonitoringSyncUpResponse response = MonitoringSyncUpResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Sync-Up request received and queued.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Failed to process gRPC Sync-Up request", e);
            responseObserver.onError(e);
        }
    }
}