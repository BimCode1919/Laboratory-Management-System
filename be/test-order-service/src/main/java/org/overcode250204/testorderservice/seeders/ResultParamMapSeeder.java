package org.overcode250204.testorderservice.seeders;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.models.entites.ResultParameterMapping;
import org.overcode250204.testorderservice.models.enums.TestOrderType;
import org.overcode250204.testorderservice.repositories.ResultParameterMappingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResultParamMapSeeder {
    private final ResultParameterMappingRepository resultParameterMappingRepository;

    @PostConstruct
    @Transactional
    public void seedResultParamMap(){
        if(resultParameterMappingRepository.count() > 0){
            log.info("ResultParameterMaps already exist. Skipping...");
            return;
        }

        // instrument id
        String dataSource = "GLOBAL";

        List<ResultParameterMapping> paramMaps = new java.util.ArrayList<>();

        paramMaps.addAll(List.of(
                build(TestOrderType.CBC, "WBC", "WHITE_BLOOD_CELL", dataSource),
                build(TestOrderType.CBC, "RBC", "RED_BLOOD_CELL", dataSource),
                build(TestOrderType.CBC, "HGB", "HEMOGLOBIN", dataSource),
                build(TestOrderType.CBC, "HCT", "HEMATOCRIT", dataSource),
                build(TestOrderType.CBC, "PLT", "PLATELET_COUNT", dataSource),
                build(TestOrderType.CBC, "MCV", "MEAN_CORPUSCULAR_VOLUME", dataSource),
                build(TestOrderType.CBC, "MCH", "MEAN_CORPUSCULAR_HGB", dataSource),
                build(TestOrderType.CBC, "MCHC", "MEAN_CORPUSCULAR_HGB_CONC", dataSource)
        ));

        paramMaps.addAll(List.of(
                build(TestOrderType.HBA1C, "HbA1C", "GLYCATED_HEMOGLOBIN", dataSource)
        ));

        paramMaps.addAll(List.of(
                build(TestOrderType.LFT, "ALT", "ALANINE_AMINOTRANSFERASE", dataSource),
                build(TestOrderType.LFT, "AST", "ASPARTATE_AMINOTRANSFERASE", dataSource),
                build(TestOrderType.LFT, "BIL", "TOTAL_BILIRUBIN", dataSource),
                build(TestOrderType.LFT, "ALP", "ALKALINE_PHOSPHATASE", dataSource),
                build(TestOrderType.LFT, "ALB", "ALBUMIN", dataSource)
        ));

        resultParameterMappingRepository.saveAll(paramMaps);
        log.info("Seeded {} ResultParameterMappings successfully.", paramMaps.size());
    }
    
    private ResultParameterMapping build(TestOrderType type, String external, String internal, String dataSource) {
        return ResultParameterMapping.builder()
                .testType(type)
                .externalParamName(external)
                .internalParamName(internal)
                .dataSource(dataSource)
                .isActivated(true)
                .build();
    }
}