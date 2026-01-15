package org.overcode250204.testorderservice.services.impls;

import com.udojava.evalex.Expression;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.events.TestResultChangedEvent;
import org.overcode250204.testorderservice.events.MonitoringPublisher;
import org.overcode250204.testorderservice.exceptions.ErrorCode;
import org.overcode250204.testorderservice.exceptions.TestOrderException;
import org.overcode250204.testorderservice.models.entites.*;
import org.overcode250204.testorderservice.models.enums.Gender;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.overcode250204.testorderservice.models.enums.TestOrderStatus;
import org.overcode250204.testorderservice.models.enums.TestResultAlertLevel;
import org.overcode250204.testorderservice.models.enums.TestResultStatus;
import org.overcode250204.testorderservice.repositories.*;
import org.overcode250204.testorderservice.services.AIReviewService;
import org.overcode250204.testorderservice.services.TestResultProcessingService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

record ConvertedUnitResult(Double value, String unit, UnitConversionMapping mapping) {
}

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultProcessingServiceImpl implements TestResultProcessingService {

    private final TestResultRawRepository rawRepository;
    private final TestResultsRepository testResultsRepository;
    private final ResultParameterMappingRepository paramMappingRepository;
    private final UnitConversionMappingRepository unitConversionRepository;
    private final FlaggingRulesRepository flaggingRulesRepository;
    private final TestOrdersRepository testOrdersRepository;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;
    private final MonitoringPublisher monitoringPublisher;
    private final AIReviewService aiReviewService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public List<TestResults> processRawResults(List<UUID> runIds) {
        if (runIds == null || runIds.isEmpty()) {
            log.warn("[Processor] No raw result IDs provided for processing.");
            return List.of();
        }

        log.info("[Processor] Starting normalization for {} raw result IDs.", runIds.size());

        // 1. Lấy tất cả raw results trong DB
        List<TestResultRaw> rawResults = rawRepository.findByRunIdInAndIsProcessedFalse(runIds);
        if (rawResults == null || rawResults.isEmpty()) {
            log.warn("[Processor] No unprocessed raw results found for runIds: {}.", runIds);
            throw new TestOrderException(ErrorCode.RAW_TEST_RESULTS_NOT_FOUND);
        }

        // 2. Pre loading
        // Tải tất cả TestOrders + Patients liên quan (1 Query)
        List<String> barcodes = rawResults.stream().map(TestResultRaw::getBarcode).distinct().toList();
        // (Bạn cần thêm 'findByBarCodeInWithPatient' vào TestOrdersRepository)
        Map<String, TestOrders> ordersMap = testOrdersRepository.findByBarCodeInWithPatient(barcodes).stream()
                .collect(Collectors.toMap(TestOrders::getBarCode, Function.identity()));
        // Tải tất cả Rules (3 Queries)
        // (Trong thực tế, 3 list này nên được cache bằng @Cacheable)
        List<ResultParameterMapping> allParamMaps = paramMappingRepository.findAll();
        List<UnitConversionMapping> allUnitMaps = unitConversionRepository.findAll();
        List<FlaggingRules> allFlagRules = flaggingRulesRepository.findAll();

        // 3. Xử lý các raw results
        List<TestResults> allProcessed = new ArrayList<>();
        List<TestResultRaw> processedRawList = new ArrayList<>(); // List để theo dõi raw nào đã xử lý

        for (TestResultRaw rawResult : rawResults) {
            // Lấy order từ Map (nhanh, 0 query)
            TestOrders testOrders = ordersMap.get(rawResult.getBarcode());
            if (testOrders == null) {
                log.warn("[Normalization] No TestOrders found for barcode={}. Skipping.", rawResult.getBarcode());
                continue;
            }

            TestResults normalized = normalizeSingleResultRow(
                    rawResult, testOrders, allParamMaps, allUnitMaps, allFlagRules
            );

            if (normalized != null) {
                allProcessed.add(normalized);
                processedRawList.add(rawResult); // Thêm vào list để cập nhật
            }
        }

        // 4. Lưu (Nếu không có gì được xử lý thì không làm gì)
        if (allProcessed.isEmpty()) {
            log.info("[Processor] No results were successfully normalized.");
            return List.of();
        }

        // 5. TÍCH HỢP AI REVIEW
        try {
            log.info("[Processor] Starting AI review for {} processed results...", allProcessed.size());
            // aiReviewService sẽ cập nhật (mutate) các object trong list 'allProcessed'
            aiReviewService.generateReview(allProcessed);
            log.info("[Processor] AI review completed.");
        } catch (Exception e) {
            log.error("[Processor] AI Review step failed. Proceeding to save results without AI review.", e);
            // (Không ném lỗi, vẫn tiếp tục lưu)
        }

        // 6. Lưu vào DB

        testResultsRepository.saveAll(allProcessed);

        updateTestOrderStatus(allProcessed);

        Map<TestResultRaw, UUID> rawToOrderMap = allProcessed.stream()
                .collect(Collectors.toMap(
                        TestResults::getRawTestResults,
                        result -> result.getTestOrder().getId()
                ));

        processedRawList.forEach(raw -> { // Chỉ cập nhật những cái đã xử lý
            raw.setIsProcessed(true);
            raw.setProcessedAt(LocalDateTime.now());
            raw.setTestOrderId(rawToOrderMap.get(raw));
        });
        rawRepository.saveAll(processedRawList);

        log.info("[Processor] Total normalized results across all runIds: {}", allProcessed.size());
        allProcessed.forEach(res ->
                log.info("[Result] param={} value={} unit={} alert={}",
                        res.getParameterName(),
                        res.getResultValue(),
                        res.getUnit(),
                        res.getAlertLevel()));

        // 7. TÍCH HỢP OUTBOX

        try {
            Set<UUID> testOrderIds = allProcessed.stream()
                    .map(res -> res.getTestOrder().getId())
                    .collect(Collectors.toSet());

            Set<UUID> patientIds = allProcessed.stream()
                    .map(res -> res.getTestOrder().getPatient().getPatientId())
                    .collect(Collectors.toSet());

            String aggregateId = runIds.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));

            Map<String, Object> logPayload = new HashMap<>();

            logPayload.put("testOrderId", testOrderIds.iterator().next());
            logPayload.put("patientId", patientIds.iterator().next());
            logPayload.put("barcode", barcodes.iterator().next());
            logPayload.put("runIdsProcessed", runIds.iterator().next());
            logPayload.put("totalNormalizedCount", allProcessed.size());
            logPayload.put("details", "Batch processing of raw results completed successfully.");

            monitoringPublisher.publishMonitoringEvent(
                    "TEST_RESULTS_PROCESSED",
                    aggregateId,
                    logPayload
            );
            log.info("[Audit] Published 'TEST_RESULTS_PROCESSED' for runIds: {}", runIds);
        } catch (Exception e) {
            log.error("[Audit] Failed to publish normalization event: {}", e.getMessage(), e);
            throw new TestOrderException(ErrorCode.FAIL_TO_CREAT_AUDIT_LOG);
        }

        // 8. TÍCH HỢP OUTBOX (Completion)
        try {
            publishCompletionEvents(allProcessed);
        } catch (Exception e) {
            log.error("Failed to publish completion event: {}", e.getMessage(), e);
            throw new TestOrderException(ErrorCode.FAIL_TO_PUBLISH_TEST_ORDER_COMPLETION);
        }

        // 9. RE-INDEX ELASTICSEARCH (Sự kiện nội bộ)
        // Lấy danh sách các Order ID duy nhất đã bị ảnh hưởng
        List<UUID> affectedOrderIds = allProcessed.stream()
                .map(result -> result.getTestOrder().getId())
                .distinct()
                .toList();

        // Phát sự kiện (NỘI BỘ) cho TestOrderIndexListener
        for (UUID orderId : affectedOrderIds) {
            log.info("[Processor] Publishing TestResultChangedEvent (Internal) for Order ID: {}", orderId);
            // Kích hoạt Listener để đồng bộ ES
            eventPublisher.publishEvent(new TestResultChangedEvent(this, orderId));
        }

        return allProcessed;
    }

    @Override
    public void markWaitingForInstrument(String barcode) {
        TestOrders order = testOrdersRepository.findByBarCode(barcode)
                .orElseThrow(() -> new TestOrderException(ErrorCode.TEST_NOT_FOUND));

        order.setStatus(TestOrderStatus.WAITING_FOR_INSTRUMENT);

        testOrdersRepository.save(order);

        log.info("[TestOrder] Marked WAITING_FOR_INSTRUMENT for barcode={}", barcode);
    }

    @Override
    public void markSyncFailed(String barcode) {
        TestOrders order = testOrdersRepository.findByBarCode(barcode)
                .orElseThrow(() -> new TestOrderException(ErrorCode.TEST_NOT_FOUND));
        order.setStatus(TestOrderStatus.SYNC_FAILED);

        testOrdersRepository.save(order);

        log.info("[TestOrder] Marked SYNC_FAILED for barcode={}", barcode);
    }

    private TestResults normalizeSingleResultRow(TestResultRaw rawResult,
                                                 TestOrders testOrders,
                                                 List<ResultParameterMapping> allParamMaps,
                                                 List<UnitConversionMapping> allUnitMaps,
                                                 List<FlaggingRules> allFlagRules) {

        try {
            String genderString = testOrders.getPatient().getGender();
            final Gender patientGender;
            if ("Nam".equalsIgnoreCase(genderString) || "Male".equalsIgnoreCase(genderString)) {
                patientGender = Gender.MALE;
            } else if ("Nữ".equalsIgnoreCase(genderString) || "Nu".equalsIgnoreCase(genderString) || "Female".equalsIgnoreCase(genderString)) {
                patientGender = Gender.FEMALE;
            } else {
                patientGender = null; // Hoặc Gender.OTHER
            }

            // Logic mapParameter
            Optional<ResultParameterMapping> paramMapOpt = mapParameter(rawResult, allParamMaps);
            if (paramMapOpt.isEmpty()) return null;
            ResultParameterMapping paramMap = paramMapOpt.get();
            String internalParamName = paramMap.getInternalParamName();

            // Logic convertUnits
            ConvertedUnitResult converted = convertUnits(rawResult, testOrders.getTestType(), allUnitMaps);
            if (converted == null) return null;

            // Logic findFlaggingRule
            FlaggingRules rule = findFlaggingRule(internalParamName, converted.unit(), patientGender, allFlagRules);

            TestResults normalized = new TestResults();
            normalized.setTestOrder(testOrders);
            normalized.setRawTestResults(rawResult);
            normalized.setParamMap(paramMap);
            normalized.setParameterName(internalParamName);
            normalized.setResultValue(converted.value());
            normalized.setUnit(converted.unit());
            normalized.setConversion(converted.mapping());
            normalized.setAlertLevel(applyFlag(converted.value(), rule));
            normalized.setCreatedAt(LocalDateTime.now());
            normalized.setStatus(TestResultStatus.PENDING);
            normalized.setAiHasIssue(false);
            normalized.setAiReviewComment(null);

            if (rule != null) {
                normalized.setFlagRule(rule);
                if (rule.getNormalLow() != null) normalized.setReferenceLow(String.valueOf(rule.getNormalLow()));
                if (rule.getNormalHigh() != null) normalized.setReferenceHigh(String.valueOf(rule.getNormalHigh()));
            }
            return normalized;

        } catch (Exception e) {
            log.error("[Normalization] Failed to normalize raw id={} due to {}", rawResult.getId(), e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    public void updateTestOrderStatus(List<TestResults> processedResults) {
        if (processedResults.isEmpty()) {
            return;
        }

        Set<UUID> affectedOrderIds = processedResults.stream()
                .map(res -> res.getTestOrder().getId())
                .collect(Collectors.toSet());

        log.info("[StatusUpdate] Attempting to mark {} Test Orders as COMPLETED.", affectedOrderIds.size());

        List<TestOrders> ordersToUpdate = testOrdersRepository.findAllById(affectedOrderIds);
        List<TestOrders> completedOrders = ordersToUpdate.stream()
                .peek(order -> {
                    order.setStatus(TestOrderStatus.COMPLETED);
                    log.debug("Order {} status set to COMPLETED.", order.getId());
                })
                .toList();

        testOrdersRepository.saveAll(completedOrders);

        log.info("[StatusUpdate] Successfully updated {} Test Orders to COMPLETED.", completedOrders.size());
    }

    private Optional<ResultParameterMapping> mapParameter(TestResultRaw raw, List<ResultParameterMapping> allParamMaps) {
        String instrumentIdStr = raw.getInstrumentId().toString();

        // Ưu tiên 1: Tìm rule khớp chính xác Instrument ID (nếu có)
        Optional<ResultParameterMapping> specificMap = allParamMaps.stream()
                .filter(map -> map.getExternalParamName().equals(raw.getRawParameter()) &&
                        map.getDataSource().equals(instrumentIdStr) && // <-- Logic cũ
                        map.getIsActivated())
                .findFirst();

        if (specificMap.isPresent()) {
            return specificMap;
        }

        // Ưu tiên 2: Fallback, tìm rule "GLOBAL" (Code mới)
        Optional<ResultParameterMapping> globalMap = allParamMaps.stream()
                .filter(map -> map.getExternalParamName().equals(raw.getRawParameter()) &&
                        "GLOBAL".equalsIgnoreCase(map.getDataSource()) && // <-- Logic mới
                        map.getIsActivated())
                .findFirst();

        if (globalMap.isEmpty()) {
            log.warn("[Mapping] No mapping (Specific or GLOBAL) found for param={} source={}", raw.getRawParameter(), instrumentIdStr);
        }
        return globalMap;
    }

    private ConvertedUnitResult convertUnits(TestResultRaw raw, TestOrderType testType, List<UnitConversionMapping> allUnitMaps) {
        try {
            String rawValue = raw.getRawValue();
            double value = Double.parseDouble(rawValue);
            String sourceUnit = raw.getRawUnit() != null ? raw.getRawUnit() : "";
            String targetUnit = sourceUnit;

            Optional<UnitConversionMapping> unitConvertOpt = allUnitMaps.stream()
                    .filter(map -> map.getTestType() == testType && map.getSourceUnit().equalsIgnoreCase(sourceUnit) && map.getIsActivated())
                    .findFirst();

            if (unitConvertOpt.isEmpty()) {
                unitConvertOpt = allUnitMaps.stream()
                        .filter(map -> map.getTestType() == null && map.getSourceUnit().equalsIgnoreCase(sourceUnit) && map.getIsActivated())
                        .findFirst();
            }

            UnitConversionMapping mappingUsed = null;

            if (unitConvertOpt.isPresent()) {
                UnitConversionMapping mapping = unitConvertOpt.get();
                mappingUsed = mapping;
                String formula = mapping.getFormula();

                if (formula != null && !formula.isEmpty()) {
                    Expression expression = new Expression(formula);
                    expression.setVariable("sourceUnit", rawValue);
                    BigDecimal result = expression.eval();
                    value = result.doubleValue();
                    targetUnit = mapping.getTargetUnit();
                }
            }

            return new ConvertedUnitResult(value, targetUnit, mappingUsed);
        } catch (Exception e) {
            log.error("[Conversion] Failed for raw id={} due to {}", raw.getId(), e.getMessage(), e);
            return null;
        }
    }

    private FlaggingRules findFlaggingRule(String parameter,
                                           String unit,
                                           Gender patientGender,
                                           List<FlaggingRules> allFlagRules) {
        // Lọc trước 1 lần
        List<FlaggingRules> rules = allFlagRules.stream()
                .filter(rule -> rule.getParameterName().equalsIgnoreCase(parameter) &&
                        rule.getUnit().equalsIgnoreCase(unit) &&
                        rule.getIsActivated())
                .toList();

        Optional<FlaggingRules> genderRule = rules.stream()
                .filter(rule -> rule.getGender() != null && rule.getGender().equals(patientGender))
                .findFirst();

        if (genderRule.isPresent()) {
            return genderRule.get();
        }

        Optional<FlaggingRules> generalRule = rules.stream()
                .filter(rule -> rule.getGender() == null)
                .findFirst();

        return generalRule.orElseGet(() -> {
            log.warn("[Flagging] No flagging rule found for param={}, unit={}, gender={}. Defaulting to unflagged.",
                    parameter, unit, patientGender);
            return null;
        });

    }

    private TestResultAlertLevel applyFlag(Double value, FlaggingRules rule) {
        if (rule == null || rule.getNormalLow() == null || rule.getNormalHigh() == null) {
            return TestResultAlertLevel.NORMAL;
        }
        if (value < rule.getNormalLow()) return TestResultAlertLevel.LOW;
        else if (value > rule.getNormalHigh()) return TestResultAlertLevel.HIGH;
        else return TestResultAlertLevel.NORMAL;
    }

    private void publishCompletionEvents(List<TestResults> allProcessedResults) {
        try {
            Map<TestOrders, List<TestResults>> resultsByOrder = allProcessedResults.stream()
                    .collect(Collectors.groupingBy(TestResults::getTestOrder));

            log.info("[Publisher] Found {} completed orders to publish events for.", resultsByOrder.size());

            for (Map.Entry<TestOrders, List<TestResults>> entry : resultsByOrder.entrySet()) {
                TestOrders testOrder = entry.getKey();
                List<TestResults> testResults = entry.getValue();

                Map<String, Object> data = new HashMap<>();
                data.put("eventId", UUID.randomUUID());
                data.put("completedAt", Instant.now().toString());
                data.put("testOrderId", testOrder.getId().toString());
                data.put("patientId", testOrder.getPatient().getPatientId().toString());
                data.put("medicalRecordId", testOrder.getMedicalRecordId().toString());
                data.put("status", testOrder.getStatus());
                data.put("testType", testOrder.getTestType());
                if (!testResults.isEmpty() && testResults.getFirst().getRawTestResults() != null) {
                    TestResultRaw firstRawResult = testResults.getFirst().getRawTestResults();
                    data.put("instrumentId", firstRawResult.getInstrumentId().toString());

                    // Xử lý JSON an toàn
                    String reagentSnapshot = firstRawResult.getReagentSnapshotJson();
                    if(reagentSnapshot != null) data.put("reagentDetails", objectMapper.readTree(reagentSnapshot));
                    String instrumentDetails = firstRawResult.getInstrumentDetailJson();
                    if(instrumentDetails != null) data.put("instrumentDetails", objectMapper.readTree(instrumentDetails));
                }

                List<Map<String, Object>> resultsPayloads = testResults.stream()
                        .map(res -> {
                            Map<String, Object> payload = new HashMap<>();
                            payload.put("parameterName", res.getParameterName());
                            payload.put("resultValue", res.getResultValue());
                            payload.put("unit", res.getUnit());
                            payload.put("referenceLow", res.getReferenceLow());
                            payload.put("referenceHigh", res.getReferenceHigh());
                            payload.put("alertLevel", res.getAlertLevel());
                            return payload;
                        }).collect(Collectors.toList());

                data.put("results", resultsPayloads);
                String payload = objectMapper.writeValueAsString(data);
                OutboxEvent outboxEvent = new OutboxEvent();
                outboxEvent.setEventType("TEST_ORDER_COMPLETED");
                outboxEvent.setCreatedAt(Instant.now());
                outboxEvent.setPayload(payload);
                outboxEvent.setAggregateType("TEST_ORDER_COMPLETED");
                outboxEvent.setAggregateId(testOrder.getOrderCode());
                outboxRepository.save(outboxEvent);
            }
        } catch (Exception e) {
            log.error("[Publisher] Failed to publish completion events at TestResultProcessingServiceImpl: {}", e.getMessage());
            throw new TestOrderException(ErrorCode.FAIL_TO_PUBLISH_TEST_ORDER_COMPLETION);
        }
    }
}
