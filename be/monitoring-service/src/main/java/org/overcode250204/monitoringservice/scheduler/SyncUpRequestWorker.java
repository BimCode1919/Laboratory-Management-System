package org.overcode250204.monitoringservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.overcode250204.monitoringservice.entities.SyncUpRequests;
import org.overcode250204.monitoringservice.enums.SyncUpRequestsStatus;
import org.overcode250204.monitoringservice.repositories.SyncUpRequestsRepo;
import org.overcode250204.monitoringservice.grpc.InstrumentSyncGrpcClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncUpRequestWorker {

    private final SyncUpRequestsRepo repo;
    private final ObjectMapper objectMapper;
    private final InstrumentSyncGrpcClient instrumentClient;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processPendingSyncRequests() {

        List<SyncUpRequests> pending = repo.findByStatus(SyncUpRequestsStatus.PENDING);
        if (pending.isEmpty()) return;

        log.info("[SyncWorker] Found {} sync requests pending...", pending.size());

        for (SyncUpRequests req : pending) {

            List<String> barcodes = parseBarcodes(req.getMessageId());

            for (String barcode : barcodes) {
                instrumentClient.syncUpByBarcode(barcode, req.getSourceService());
            }

            req.setStatus(SyncUpRequestsStatus.COMPLETED);
            repo.save(req);
        }
    }

    private List<String> parseBarcodes(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.error("Failed to parse barcode list from SyncUpRequest", e);
            return List.of();
        }
    }
}
