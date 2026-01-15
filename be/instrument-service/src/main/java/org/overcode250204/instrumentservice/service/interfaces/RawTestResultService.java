package org.overcode250204.instrumentservice.service.interfaces;

import org.overcode250204.instrumentservice.entity.RawTestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RawTestResultService {

    RawTestResult getRawTestResultById(String id);

    RawTestResult createRawTestResult(RawTestResult rawTestResult);

    RawTestResult updateRawTestResult(String id, RawTestResult rawTestResult);

    Page<RawTestResult> getAllResults(Pageable pageable);

    Page<RawTestResult> getAllResultsBackupTrue(Pageable pageable);

    Page<RawTestResult> getAllResultsBackupfalse(Pageable pageable);

    Page<RawTestResult> getByInstrument(UUID instrumentId, Pageable pageable);

    void deleteByRunIdAndBarcode(UUID runId ,String barcode, UUID userId);

    void autoCleanupBackedUpResults();

    Page<RawTestResult> getByInstrumentBackupTrue(UUID instrumentId, Pageable pageable);

    Page<RawTestResult> getByInstrumentBackupFalse(UUID instrumentId, Pageable pageable);
}
