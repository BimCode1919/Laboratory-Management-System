package org.overcode250204.testorderservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.overcode250204.testorderservice.models.entites.TestResults;

import java.util.List;
import java.util.UUID;

public interface TestResultProcessingService {
    List<TestResults> processRawResults(List<UUID> runIds);

    void markWaitingForInstrument(String barcode);

    void markSyncFailed(String barcode);
}
