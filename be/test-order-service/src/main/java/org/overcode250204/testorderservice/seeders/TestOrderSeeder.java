package org.overcode250204.testorderservice.seeders;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.overcode250204.testorderservice.models.entites.PatientReference;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.models.enums.TestOrderPriority;
import org.overcode250204.testorderservice.models.enums.TestOrderStatus;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.overcode250204.testorderservice.repositories.PatientReferenceRepository;
import org.overcode250204.testorderservice.repositories.TestOrdersRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestOrderSeeder {
//    private final TestOrdersRepository testOrdersRepository;
//    private final PatientReferenceRepository patientReferenceRepository;
//    private final Faker faker = new Faker(new Locale("vi"));
//
//    @PostConstruct
//    @Transactional
//    public void seedTestOrders() {
//    if (testOrdersRepository.count() > 0) {
//        log.info("TestOrders already exist. Skipping...");
//        return;
//    }
//
//    // Check patients tồn tại
//    List<PatientReference> patients = patientReferenceRepository.findAll();
//    if (patients.isEmpty()) {
//        log.warn("No PatientReferences found. Please seed patients first.");
//        return; // hoặc throw exception nếu strict
//    }
//
//    List<TestOrders> orders = new ArrayList<>();
//    for (int i = 0; i < 20; i++) { // seed 20, tăng nếu cần
//        orders.add(buildTestOrder(i + 1, patients));
//    }
//
//    testOrdersRepository.saveAll(orders);
//    log.info("Seeded {} TestOrders successfully.", orders.size());

    // Optional: Trigger reindex Elasticsearch nếu có service
    // reindexService.reindexAll();
}

//private TestOrders buildTestOrder(int index, List<PatientReference> patients) {
//    // Random patient
//    PatientReference randomPatient = patients.get(faker.number().numberBetween(0, patients.size()));
//
//    String orderCode = "TO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // unique, e.g., TO-ABC12345
//    String barCode = "BAR-" + String.format("%06d", index); // unique padded, e.g., BAR-000001
//
//    TestOrderType type = faker.options().option(TestOrderType.values()); // random enum
//    TestOrderPriority priority = faker.options().option(TestOrderPriority.values());
//    TestOrderStatus status = faker.options().option(TestOrderStatus.values());
//
//    LocalDateTime createdAt = LocalDateTime.now().minusDays(faker.number().numberBetween(1, 90)); // past 1-90 days
//
//        // Reviewed fields chỉ nếu status COMPLETED or REVIEWED
//        UUID reviewedBy = (status == TestOrderStatus.COMPLETED || status == TestOrderStatus.REVIEWED) ? UUID.randomUUID() : null;
//        LocalDateTime reviewedAt = reviewedBy != null ? createdAt.plusHours(faker.number().numberBetween(1, 24)) : null;
//
//        return TestOrders.builder()
//                .orderCode(orderCode)
//                .barCode(barCode)
//                .testType(type)
//                .patient(randomPatient) // link existing patient
//                .medicalRecordId(UUID.randomUUID()) // mock
//                .priority(priority)
//                .status(status)
//                .createdBy(UUID.randomUUID()) // mock user
//                .createdAt(createdAt) // override PrePersist để mock old dates
//                .reviewedBy(reviewedBy)
//                .reviewedAt(reviewedAt)
//                .notes(faker.lorem().paragraph() + " (ghi chú tiếng Việt với dấu cho test order " + index + ").") // mock notes VN
//                .isDeleted(false)
//                .build();
//    }
//}