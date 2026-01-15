package org.overcode250204.testorderservice.seeders;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.models.entites.UnitConversionMapping;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.overcode250204.testorderservice.repositories.UnitConversionMappingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class UnitConversionMapSeeder {
    private final UnitConversionMappingRepository unitConversionMappingRepository;

    @PostConstruct
    @Transactional
    public void seedUnitConversionMappings() {
        if (unitConversionMappingRepository.count() > 0) {
            log.info("UnitConversionMapping already exists. Skipping...");
            return;
        }

        List<UnitConversionMapping> mappings = new java.util.ArrayList<>();

        // --- CBC Mappings ---
        mappings.addAll(List.of(
                build(TestOrderType.CBC, "GLOBAL", "cells/μL", "cells/μL", "sourceUnit * 1.0",
                        "Direct mapping for White Blood Cells and Platelets (Conventional)."),
                build(TestOrderType.CBC, "GLOBAL", "cells/uL", "cells/μL", "sourceUnit * 1.0",
                        "Direct mapping for White Blood Cells and Platelets (Handle uL variant)."),
                build(TestOrderType.CBC, "GLOBAL", "million/μL", "million/μL", "sourceUnit * 1.0",
                        "Direct mapping for Red Blood Cells (Conventional)."),
                build(TestOrderType.CBC, "GLOBAL", "million/uL", "million/μL", "sourceUnit * 1.0",
                        "Direct mapping for Red Blood Cells (Handle uL variant)."),
                build(TestOrderType.CBC, "GLOBAL", "g/dL", "g/dL", "sourceUnit * 1.0",
                        "Direct mapping for Hemoglobin and MCHC."),
                build(TestOrderType.CBC, "GLOBAL", "%", "%", "sourceUnit * 1.0",
                        "Direct mapping for Hematocrit."),
                build(TestOrderType.CBC, "GLOBAL", "fL", "fL", "sourceUnit * 1.0",
                        "Direct mapping for MCV."),
                build(TestOrderType.CBC, "GLOBAL", "pg", "pg", "sourceUnit * 1.0",
                        "Direct mapping for MCH."),
                build(TestOrderType.CBC, "GLOBAL", "g/dL", "g/L", "sourceUnit * 10.0",
                        "Converts Hemoglobin and MCHC from g/dL to g/L using formula (x 10)."),

                build(TestOrderType.CBC, "GLOBAL", "10^9/L", "cells/μL", "sourceUnit * 1.0",
                        "Converts WBC/Platelets from 10^9/L (SI) to cells/μL (Conventional) - multiplier is 1."),
                build(TestOrderType.CBC, "GLOBAL", "10^12/L", "million/μL", "sourceUnit * 1.0",
                        "Converts RBC from 10^12/L (SI) to million/μL (Conventional) - multiplier is 1.")
        ));

        // --- GLOBAL Mappings ---
        mappings.addAll(List.of(
                build(null, "GLOBAL", "mg/dL", "mmol/L", "sourceUnit * 0.0555",
                        "Common conversion for Glucose (mg/dL to mmol/L)."),
                build(null, "GLOBAL", "IU/L", "U/L", "sourceUnit * 1.0",
                        "Ensures IU/L is mapped to U/L (often equivalent)."),
                build(TestOrderType.LFT, "GLOBAL", "U/L", "kat/L", "sourceUnit * 0.0000000166667", // Equivalent to / 60,000,000
                        "Converts enzyme activity from U/L to kat/L using formula.")
        ));

        // --- NEW LFT and HBA1C Mappings ---

        // LFT - TOTAL_BILIRUBIN (SI to Conventional)
        // Formula: mg/dL = µmol/L / 17.1 -> (sourceUnit * 1/17.1) approx 0.05848
        mappings.add(build(TestOrderType.LFT, "GLOBAL", "µmol/L", "mg/dL", "sourceUnit * 0.0585",
                "Converts Total Bilirubin from µmol/L (SI) to mg/dL (Conventional)."));

        // LFT - TOTAL_BILIRUBIN (Conventional to SI)
        mappings.add(build(TestOrderType.LFT, "GLOBAL", "mg/dL", "µmol/L", "sourceUnit * 17.1",
                "Converts Total Bilirubin from mg/dL (Conventional) to µmol/L (SI)."));

        // LFT - Enzyme and Albumin Identity Mappings (ALT, AST, ALP, ALB)
        mappings.add(build(TestOrderType.LFT, "GLOBAL", "U/L", "U/L", "sourceUnit * 1.0",
                "Identity mapping for ALT, AST, and ALP enzymes."));
        mappings.add(build(TestOrderType.LFT, "GLOBAL", "g/dL", "g/dL", "sourceUnit * 1.0",
                "Identity mapping for Albumin."));

        // HBA1C - Identity Mapping
        mappings.add(build(TestOrderType.HBA1C, "GLOBAL", "%", "%", "sourceUnit * 1.0",
                "Identity mapping for HbA1C (Percentage)."));

        unitConversionMappingRepository.saveAll(mappings);
        log.info("Seeded {} UnitConversionMappings successfully.", mappings.size());
    }

    public UnitConversionMapping build(TestOrderType type,
                                       String dataSource,
                                       String sourceUnit,
                                       String targetUnit,
                                       String formula,
                                       String description) {
        return UnitConversionMapping.builder()
                .testType(type)
                .dataSource(dataSource)
                .sourceUnit(sourceUnit)
                .targetUnit(targetUnit)
                .formula(formula)
                .description(description)
                .isActivated(true)
                .build();
    }
}
