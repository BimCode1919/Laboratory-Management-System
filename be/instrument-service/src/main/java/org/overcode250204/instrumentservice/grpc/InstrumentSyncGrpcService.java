package org.overcode250204.instrumentservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.overcode250204.common.grpc.InstrumentSyncServiceGrpc;
import org.overcode250204.common.grpc.InstrumentSyncUpRequest;
import org.overcode250204.common.grpc.RawTestResultMessage;
import org.overcode250204.instrumentservice.entity.RawTestResult;
import org.overcode250204.instrumentservice.grpc.mapper.RawTestResultMapper;
import org.overcode250204.instrumentservice.repository.RawTestResultRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@GrpcService
@RequiredArgsConstructor
@Slf4j
public class InstrumentSyncGrpcService extends InstrumentSyncServiceGrpc.InstrumentSyncServiceImplBase {

    private final RawTestResultRepository rawTestResultRepository;

    @Override
    public void syncUpTestResults(InstrumentSyncUpRequest request, StreamObserver<RawTestResultMessage> responseObserver) {
        try {
            log.info("Sync-up request received for instrument={}, testOrderId={}, barcode={}",
                    request.getInstrumentId(), request.getTestOrderId(), request.getBarcode());

            List<RawTestResult> results = new ArrayList<>();

            if (!request.getBarcode().isBlank()) {
                rawTestResultRepository.findByBarcode(request.getBarcode())
                        .ifPresent(results::add);

            } else if (!request.getRunIdsList().isEmpty()) {
                for (String id : request.getRunIdsList()) {
                    try {
                        results.addAll(rawTestResultRepository.findByRunId(UUID.fromString(id)));
                    } catch (IllegalArgumentException ex) {
                        log.warn("Invalid runId: {}", id);
                    }
                }
            } else {
                int limit = request.getLimit() > 0 ? request.getLimit() : 100;
                results = rawTestResultRepository.findTopN(limit);
            }

            log.info("Found {} raw test results for instrument {}", results.size(), request.getInstrumentId());

            // Stream từng record về client (Monitoring Service)
            for (RawTestResult result : results) {
                RawTestResultMessage msg = RawTestResultMapper.toGrpc(result);
                responseObserver.onNext(msg);
            }

            responseObserver.onCompleted();
            log.info(" Sync-up stream completed for instrument {}", request.getInstrumentId());

        } catch (Exception e) {
            log.error("Error during gRPC sync-up: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }
}
