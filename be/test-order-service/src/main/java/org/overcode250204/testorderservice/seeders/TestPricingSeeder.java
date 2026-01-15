package org.overcode250204.testorderservice.seeders;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.models.entites.TestPricing;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.overcode250204.testorderservice.repositories.TestPricingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestPricingSeeder {
    private final TestPricingRepository testPricingRepository;

    @PostConstruct
    @Transactional
    public void seedTestPricings() {
        if (testPricingRepository.count() > 0) {
            log.info("TestPricings already exist. Skipping...");
            return;
        }

        // vnd prices
        List<TestPricing> pricings = List.of(
                build(TestOrderType.CBC, 350000.00),     // Complete Blood Count: 350,000 VND
                build(TestOrderType.LFT, 500000.00),     // Liver Function Test: 500,000 VND
                build(TestOrderType.HBA1C, 420000.00)    // Glycated Hemoglobin: 420,000 VND
        );

        testPricingRepository.saveAll(pricings);
        log.info("Seeded {} TestPricings successfully.", pricings.size());
    }

    private TestPricing build(TestOrderType type, Double price) {
        return TestPricing.builder()
                .testType(type)
                .price(price)
                .build();
    }
}
