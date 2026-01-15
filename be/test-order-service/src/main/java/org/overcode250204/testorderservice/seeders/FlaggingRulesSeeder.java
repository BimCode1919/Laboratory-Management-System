package org.overcode250204.testorderservice.seeders;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.models.entites.FlaggingRules;
import org.overcode250204.testorderservice.models.enums.Gender;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.overcode250204.testorderservice.repositories.FlaggingRulesRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlaggingRulesSeeder {
    private final FlaggingRulesRepository flaggingRulesRepository;

    private static final double FACTOR_U_TO_KAT = 0.0000000166667;
    private static final double FACTOR_MGDL_TO_UMOL = 17.1;

    @PostConstruct
    @Transactional
    public void seedFlaggingRules() {
        if (flaggingRulesRepository.count() > 0) {
            log.info("FlaggingRules already exists. Skipping...");
            return;
        }

        List<FlaggingRules> rules = new ArrayList<>();

        // --- CBC Rules (Existing) ---
        rules.addAll(List.of(
                build(TestOrderType.CBC, "WHITE_BLOOD_CELL", "cells/μL", null, 4000.0, 10000.0, "WBC Standard Range"),
                build(TestOrderType.CBC, "RED_BLOOD_CELL", "million/μL", Gender.MALE, 4.7, 6.1, "RBC Male Standard Range"),
                build(TestOrderType.CBC, "RED_BLOOD_CELL", "million/μL", Gender.FEMALE, 4.2, 5.4, "RBC Female Standard Range"),
                build(TestOrderType.CBC, "HEMOGLOBIN", "g/dL", Gender.MALE, 14.0, 18.0, "Hb Male Standard Range"),
                build(TestOrderType.CBC, "HEMOGLOBIN", "g/dL", Gender.FEMALE, 12.0, 16.0, "Hb Female Standard Range"),
                build(TestOrderType.CBC, "HEMATOCRIT", "%", Gender.MALE, 42.0, 52.0, "Hct Male Standard Range"),
                build(TestOrderType.CBC, "HEMATOCRIT", "%", Gender.FEMALE, 37.0, 47.0, "Hct Female Standard Range"),
                build(TestOrderType.CBC, "PLATELET_COUNT", "cells/μL", null, 150000.0, 350000.0, "Platelet Standard Range"),
                build(TestOrderType.CBC, "MEAN_CORPUSCULAR_VOLUME", "fL", null, 80.0, 100.0, "MCV Standard Range"),
                build(TestOrderType.CBC, "MEAN_CORPUSCULAR_HGB", "pg", null, 27.0, 33.0, "MCH Standard Range"),
                build(TestOrderType.CBC, "MEAN_CORPUSCULAR_HGB_CONC", "g/dL", null, 32.0, 36.0, "MCHC Standard Range")
        ));

        // --- LFT Rules (With SI Variants) ---

        // Enzymes: U/L and kat/L
        addEnzymeRules(rules, "ASPARTATE_AMINOTRANSFERASE", 10.0, 40.0);
        addEnzymeRules(rules, "ALANINE_AMINOTRANSFERASE", 10.0, 40.0);
        addEnzymeRules(rules, "ALKALINE_PHOSPHATASE", 40.0, 150.0);

        // Bilirubin: mg/dL and µmol/L
        addBilirubinRules(rules, "TOTAL_BILIRUBIN", 0.2, 1.2);

        // Albumin: g/dL (SI is g/L, usually factor 10)
        // Adding manual g/L variant for completeness just in case
        rules.add(build(TestOrderType.LFT, "ALBUMIN", "g/dL", null, 3.5, 5.0, "Albumin Conventional"));
        rules.add(build(TestOrderType.LFT, "ALBUMIN", "g/L", null, 35.0, 50.0, "Albumin SI (g/L)"));

        // --- HBA1C ---
        rules.add(build(TestOrderType.HBA1C, "GLYCATED_HEMOGLOBIN", "%", null, 4.0, 5.6, "HbA1c Conventional"));
        // Note: IF you decide to use IFCC units (mmol/mol) for HbA1c later, add that here.

        flaggingRulesRepository.saveAll(rules);
        log.info("Seeded {} FlaggingRules successfully.", rules.size());
    }

    private void addEnzymeRules(List<FlaggingRules> list, String param, Double low, Double high) {
        list.add(build(TestOrderType.LFT, param, "U/L", null, low, high, param + " (Conventional)"));

        Double lowSI = low * FACTOR_U_TO_KAT;
        Double highSI = high * FACTOR_U_TO_KAT;

        list.add(build(TestOrderType.LFT, param, "kat/L", null, lowSI, highSI, param + " (SI - kat/L)"));
    }

    private void addBilirubinRules(List<FlaggingRules> list, String param, Double low, Double high) {
        list.add(build(TestOrderType.LFT, param, "mg/dL", null, low, high, param + " (Conventional)"));

        Double lowSI = low * FACTOR_MGDL_TO_UMOL;
        Double highSI = high * FACTOR_MGDL_TO_UMOL;

        list.add(build(TestOrderType.LFT, param, "µmol/L", null, lowSI, highSI, param + " (SI - µmol/L)"));
    }

    private FlaggingRules build(TestOrderType type, String param, String unit, Gender gender, Double low, Double high, String desc) {
        return FlaggingRules.builder()
                .testType(type)
                .parameterName(param)
                .unit(unit)
                .gender(gender)
                .normalLow(low)
                .normalHigh(high)
                .description(desc)
                .isActivated(true)
                .build();
    }
}
