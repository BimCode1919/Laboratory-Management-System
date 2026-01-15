package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.models.entites.UnitConversionMapping;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitConversionMappingRepository extends JpaRepository<UnitConversionMapping, UUID> {
    List<UnitConversionMapping> findByIsActivatedTrue();

    Optional<UnitConversionMapping> findFirstBySourceUnitIgnoreCaseAndIsActivatedTrue(String sourceUnit);

    Optional<UnitConversionMapping> findFirstBySourceUnitIgnoreCaseAndFormulaIsNotNullAndIsActivatedTrue(String sourceUnit);

    Optional<UnitConversionMapping> findFirstByTestTypeAndSourceUnitIgnoreCaseAndIsActivatedTrue(TestOrderType testType, String sourceUnit);
}
