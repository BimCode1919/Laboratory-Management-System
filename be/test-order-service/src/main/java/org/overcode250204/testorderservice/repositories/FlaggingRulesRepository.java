package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.models.entites.FlaggingRules;
import org.overcode250204.testorderservice.models.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FlaggingRulesRepository extends JpaRepository<FlaggingRules, UUID> {
    List<FlaggingRules> findByParameterNameIgnoreCaseAndUnitIgnoreCaseAndGenderAndIsActivatedTrue(
            String parameter, String unit, Gender gender
    );
}
