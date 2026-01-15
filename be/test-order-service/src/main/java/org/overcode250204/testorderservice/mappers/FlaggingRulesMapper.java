package org.overcode250204.testorderservice.mappers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.dtos.FlaggingRulesDTO;
import org.overcode250204.testorderservice.models.entites.FlaggingRules;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlaggingRulesMapper {
    public static FlaggingRulesDTO toDTO(FlaggingRules entity) {
        if (entity == null) return null;

        FlaggingRulesDTO dto = new FlaggingRulesDTO();
        dto.setId(entity.getId());
        dto.setParameterName(entity.getParameterName());
        dto.setUnit(entity.getUnit());
        dto.setGender(entity.getGender());
        dto.setNormalLow(entity.getNormalLow());
        dto.setNormalHigh(entity.getNormalHigh());
        dto.setDescription(entity.getDescription());
        dto.setIsActivated(entity.getIsActivated());
        return dto;
    }

    /**
     * Convert DTO -> Entity
     */
    public static FlaggingRules toEntity(FlaggingRulesDTO dto) {
        if (dto == null) return null;

        FlaggingRules entity = new FlaggingRules();
        entity.setId(dto.getId());
        entity.setParameterName(dto.getParameterName());
        entity.setUnit(dto.getUnit());
        entity.setGender(dto.getGender());
        entity.setNormalLow(dto.getNormalLow());
        entity.setNormalHigh(dto.getNormalHigh());
        entity.setDescription(dto.getDescription());
        entity.setIsActivated(dto.getIsActivated());
        return entity;
    }
}
