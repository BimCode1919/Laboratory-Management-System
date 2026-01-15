package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.models.entites.TestPricing;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TestPricingRepository extends JpaRepository<TestPricing, UUID> {
    Optional<TestPricing> findByTestType(TestOrderType testType);
}
