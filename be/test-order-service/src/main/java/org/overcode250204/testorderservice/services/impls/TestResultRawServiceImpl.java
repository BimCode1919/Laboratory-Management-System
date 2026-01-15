package org.overcode250204.testorderservice.services.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.dtos.HL7TestResult;
import org.overcode250204.testorderservice.dtos.TestResultItem;
import org.overcode250204.testorderservice.models.entites.TestResultRaw;
import org.overcode250204.testorderservice.repositories.TestOrdersRepository;
import org.overcode250204.testorderservice.repositories.TestResultRawRepository;
import org.overcode250204.testorderservice.services.TestResultRawService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultRawServiceImpl implements TestResultRawService {
    private final TestResultRawRepository testResultRawRepository;
    private final TestOrdersRepository testOrdersRepository;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
    private static final ZoneId LOCAL_ZONE = ZoneId.systemDefault();

    @Override
    @Transactional
    public List<TestResultRaw> saveRawTestResults(Map<String, Object> payload, HL7TestResult parsedResults) {

        String timestampString = (String) payload.get("timestamp");
        LocalDateTime instrumentTimestamp = null;
        if (timestampString != null) {
            try {
                instrumentTimestamp = LocalDateTime.parse(timestampString, TIMESTAMP_FORMATTER);
            } catch (Exception e) {
                log.warn("Could not parse instrument timestamp: " + timestampString + ". Error: " + e.getMessage());
            }
        }
        UUID runId = UUID.fromString((String) payload.get("runId"));
        UUID instrumentId = UUID.fromString((String) payload.get("instrumentCode"));
        String barcode = (String) payload.get("barcode");

        UUID testOrderId = null;
        if (barcode != null && !barcode.trim().isEmpty()) {
            try {
                testOrderId = testOrdersRepository.findTestOrderIdByBarCode(barcode)
                        .orElse(null);

                if (testOrderId == null) {
                    log.warn("Test Order not found for barcode: {}. Raw result will be saved without a test_order_id.", barcode);
                }
            } catch (Exception e) {
                log.error("Error looking up Test Order ID by barcode: {}", barcode, e);
            }
        }

        String reagentSnapshot = (String) payload.get("reagentSnapshot");
        String instrumentDetails = (String) payload.get("instrumentDetails");

        List<TestResultRaw> entitiesToSave = new ArrayList<>();
        for (TestResultItem item : parsedResults.getTestResults()) {
            TestResultRaw entity = new TestResultRaw();

            entity.setRunId(runId);
            entity.setInstrumentId(instrumentId);
            entity.setBarcode(barcode);
            entity.setInstrumentTimestamp(instrumentTimestamp);

            entity.setRawParameter(item.getParameter());
            entity.setRawValue(item.getValue());
            entity.setRawUnit(item.getUnit());
            entity.setRawFlag(item.getFlag());
            entity.setReagentSnapshotJson(reagentSnapshot);
            entity.setInstrumentDetailJson(instrumentDetails);
            entity.setTestOrderId(testOrderId);
            entitiesToSave.add(entity);
        }

        return testResultRawRepository.saveAll(entitiesToSave);
    }
}
