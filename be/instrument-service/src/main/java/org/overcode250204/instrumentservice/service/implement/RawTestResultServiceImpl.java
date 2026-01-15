package org.overcode250204.instrumentservice.service.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.entity.RawTestResult;
import org.overcode250204.instrumentservice.exception.ErrorCode;
import org.overcode250204.instrumentservice.exception.InstrumentException;
import org.overcode250204.instrumentservice.repository.RawTestResultRepository;
import org.overcode250204.instrumentservice.service.interfaces.EventLogService;
import org.overcode250204.instrumentservice.service.interfaces.RawTestResultService;
import org.overcode250204.instrumentservice.events.SystemEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RawTestResultServiceImpl implements RawTestResultService {

    private final RawTestResultRepository repository;
    private final EventLogService eventLogService;
    private final SystemEventPublisher eventPublisher;

    @Override
    public RawTestResult getRawTestResultById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new InstrumentException(ErrorCode.RESULT_NOT_FOUND));
    }

    @Override
    public RawTestResult createRawTestResult(RawTestResult rawTestResult) {
        return repository.save(rawTestResult);
    }

    @Override
    public RawTestResult updateRawTestResult(String id, RawTestResult rawTestResult) {
        RawTestResult existingResult = getRawTestResultById(id);
        // You can add more specific update logic here if needed
        existingResult.setRunId(rawTestResult.getRunId());
        existingResult.setInstrumentId(rawTestResult.getInstrumentId());
        existingResult.setBarcode(rawTestResult.getBarcode());
        existingResult.setCreatedBy(rawTestResult.getCreatedBy());
        existingResult.setRawData(rawTestResult.getRawData());
        existingResult.setHl7Message(rawTestResult.getHl7Message());
        existingResult.setStatus(rawTestResult.getStatus());
        existingResult.setPublishedAt(rawTestResult.getPublishedAt());
        existingResult.setBackedUp(rawTestResult.isBackedUp());
        existingResult.setTestType(rawTestResult.getTestType());
        existingResult.setErrorMessage(rawTestResult.getErrorMessage());
        return repository.save(existingResult);
    }

    @Override
    public Page<RawTestResult> getAllResults(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<RawTestResult> getAllResultsBackupTrue(Pageable pageable) {
        return repository.findAllByBackedUpTrue(true, pageable);
    }

    @Override
    public Page<RawTestResult> getAllResultsBackupfalse(Pageable pageable) {
        return repository.findAllByBackedUpFalse(false, pageable);
    }


    @Override
    public Page<RawTestResult> getByInstrumentBackupTrue(UUID instrumentId, Pageable pageable) {
        return repository.findByInstrumentIdAndBackedUpTrue(instrumentId, true, pageable);
    }

    @Override
    public Page<RawTestResult> getByInstrumentBackupFalse(UUID instrumentId, Pageable pageable) {
        return repository.findByInstrumentIdAndBackedUpFalse(instrumentId, false, pageable);
    }

    @Override
    public Page<RawTestResult> getByInstrument(UUID instrumentId, Pageable pageable) {
        return repository.findByInstrumentId(instrumentId, pageable);
    }


    @Transactional
    @Override
    public void deleteByRunIdAndBarcode(UUID runId ,String barcode, UUID userId) {
        RawTestResult result = repository.findByRunIdAndBarcode(runId, barcode)
                .orElseThrow(() -> new InstrumentException(ErrorCode.RESULT_NOT_FOUND));

        if (!result.isBackedUp()) {
            throw new InstrumentException(ErrorCode.RESULT_NOT_BACKED_UP);
        }

        repository.delete(result);
        log.info("[ManualDelete] Deleted raw result barcode={} by user={}", barcode, userId);

        eventLogService.logEvent(result.getInstrumentId(),
                "RAW_RESULT_DELETED_MANUAL",
                "Deleted raw result barcode=" + barcode,
                userId);

        eventPublisher.publishMonitoringEvent("INSTRUMENT_RAW_DELETED", Map.of(
                "barcode", barcode,
                "instrumentId", result.getInstrumentId().toString(),
                "runId", runId.toString(),
                "deletedBy", userId.toString(),
                "deletedAt", LocalDateTime.now().toString(),
                "mode", "MANUAL"
        ));
    }

    @Override
    public void autoCleanupBackedUpResults() {
        long totalBackedUp = repository.countByBackedUpTrue();

        if (totalBackedUp == 0) {
            log.info("[AutoCleanup] No backed-up raw results found. Skipping cleanup.");
            return;
        }

        LocalDateTime start = LocalDateTime.now();
        repository.deleteAllByBackedUpTrue();
        LocalDateTime end = LocalDateTime.now();

        long durationMs = java.time.Duration.between(start, end).toMillis();

        log.info("[AutoCleanup] Deleted {} backed-up raw results in {} ms", totalBackedUp, durationMs);

        eventLogService.logEvent(
                null,
                "RAW_RESULT_AUTO_CLEANUP",
                String.format("System cleanup: deleted %d backed-up results in %d ms.", totalBackedUp, durationMs),
                null
        );


        eventPublisher.publishMonitoringEvent("RAW_RESULT_AUTO_CLEANUP", Map.of(
                "deletedCount", totalBackedUp,
                "durationMs", durationMs,
                "deletedAt", end.toString(),
                "deletedBy", "SYSTEM"
        ));
    }

}
