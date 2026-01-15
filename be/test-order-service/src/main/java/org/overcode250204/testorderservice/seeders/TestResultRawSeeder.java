package org.overcode250204.testorderservice.seeders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.overcode250204.testorderservice.events.RawTestResultProducer;
import org.overcode250204.testorderservice.models.entites.TestResultRaw;
import org.overcode250204.testorderservice.repositories.TestResultRawRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestResultRawSeeder {
//    private final TestResultRawRepository testResultRawRepository;
//    private final RawTestResultProducer rawTestResultProducer;
//
//    // Lấy từ ResultParamMapSeeder
//    private static final UUID INSTRUMENT_ID = UUID.fromString("d4a2e4c1-3ae4-4e50-9a32-fe2e2221e321");
//
//    @PostConstruct
//    @Transactional
//    public void seedTestResultRaw() {
//        if (testResultRawRepository.count() > 0) {
//            log.info("TestResultRaw already exists. Skipping...");
//            return;
//        }
//
//        log.info("Seeding TestResultRaw data...");
//        List<TestResultRaw> rawResults = new ArrayList<>();
//
//        // UUID cho 1 lần chạy (run) của máy
//        UUID runId = UUID.randomUUID();
//
//        // Kịch bản 1: Bệnh nhân bình thường (BAR-000001)
//        // Dựa trên FlaggingRules (ví dụ: Male Adult)
//        rawResults.addAll(List.of(
//                build(runId, INSTRUMENT_ID, "BAR-000001", "WBC", "7500", "cells/uL", null), // Range: 4k-10k
//                build(runId, INSTRUMENT_ID, "BAR-000001", "RBC", "5.0", "million/uL", null), // Range: 4.7-6.1
//                build(runId, INSTRUMENT_ID, "BAR-000001", "HGB", "15.0", "g/dL", null),     // Range: 14-18
//                build(runId, INSTRUMENT_ID, "BAR-000001", "HCT", "45.0", "%", null),        // Range: 42-52
//                build(runId, INSTRUMENT_ID, "BAR-000001", "PLT", "250000", "cells/uL", null), // Range: 150k-350k
//                build(runId, INSTRUMENT_ID, "BAR-000001", "MCV", "90.0", "fL", null),       // Range: 80-100
//                build(runId, INSTRUMENT_ID, "BAR-000001", "MCH", "30.0", "pg", null),       // Range: 27-33
//                build(runId, INSTRUMENT_ID, "BAR-000001", "MCHC", "33.0", "g/dL", null)      // Range: 32-36
//        ));
//
//        // Kịch bản 2: Bệnh nhân thiếu máu (BAR-000002)
//        // Dựa trên FlaggingRules (ví dụ: Female Adult)
//        rawResults.addAll(List.of(
//                build(runId, INSTRUMENT_ID, "BAR-000002", "WBC", "6000", "cells/uL", null), // Range: 4k-10k
//                build(runId, INSTRUMENT_ID, "BAR-000002", "RBC", "3.5", "million/uL", "L"),  // Range: 4.2-5.4 (LOW)
//                build(runId, INSTRUMENT_ID, "BAR-000002", "HGB", "10.0", "g/dL", "L"),      // Range: 12-16 (LOW)
//                build(runId, INSTRUMENT_ID, "BAR-000002", "HCT", "30.0", "%", "L"),         // Range: 37-47 (LOW)
//                build(runId, INSTRUMENT_ID, "BAR-000002", "PLT", "200000", "cells/uL", null), // Range: 150k-350k
//                build(runId, INSTRUMENT_ID, "BAR-000002", "MCV", "85.0", "fL", null),       // Range: 80-100
//                build(runId, INSTRUMENT_ID, "BAR-000002", "MCH", "28.0", "pg", null),       // Range: 27-33
//                build(runId, INSTRUMENT_ID, "BAR-000002", "MCHC", "33.0", "g/dL", null)      // Range: 32-36
//        ));
//
//        // Kịch bản 3: Bệnh nhân nhiễm trùng (BAR-000003)
//        rawResults.addAll(List.of(
//                build(runId, INSTRUMENT_ID, "BAR-000003", "WBC", "15000", "cells/uL", "H"), // Range: 4k-10k (HIGH)
//                build(runId, INSTRUMENT_ID, "BAR-000003", "RBC", "4.8", "million/uL", null),
//                build(runId, INSTRUMENT_ID, "BAR-000003", "HGB", "14.5", "g/dL", null)
//                // (Giả sử máy chỉ gửi các kết quả WBC, RBC, HGB cho lần chạy này)
//        ));
//
//        // 1. Lưu dữ liệu thô vào database
//        testResultRawRepository.saveAll(rawResults);
//        log.info("Seeded {} TestResultRaw records successfully.", rawResults.size());
//
//        // 2. Kích hoạt kafka producer để gửi dữ liệu lên Kafka
//        rawTestResultProducer.sendRawResultsReceived(Collections.singletonList(runId));
//        log.info("Triggering Kafka producer to process seeded raw data for runId: {}", runId);
//
//    }
//
//    private TestResultRaw build(UUID runId, UUID instrumentId, String barcode,
//                                String rawParameter, String rawValue, String rawUnit,
//                                String rawFlag) {
//
//        TestResultRaw raw = new TestResultRaw();
//        raw.setRunId(runId);
//        raw.setInstrumentId(instrumentId);
//        raw.setBarcode(barcode);
//        raw.setRawParameter(rawParameter);
//        raw.setRawValue(rawValue);
//        raw.setRawUnit(rawUnit);
//        raw.setRawFlag(rawFlag);
//        raw.setInstrumentTimestamp(LocalDateTime.now().minusMinutes(5)); // Giả sử máy gửi 5 phút trước
//        raw.setReceivedAt(LocalDateTime.now());
//        raw.setIsProcessed(false); // Quan trọng: chưa được xử lý
//        raw.setProcessedAt(null);
//        raw.setTestOrderId(null); // Quan trọng: chưa được liên kết
//        return raw;
//    }
}
