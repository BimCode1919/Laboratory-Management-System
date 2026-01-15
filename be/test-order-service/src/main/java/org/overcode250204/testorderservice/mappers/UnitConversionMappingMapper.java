package org.overcode250204.testorderservice.mappers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.dtos.UnitConversionMappingDTO;
import org.overcode250204.testorderservice.models.entites.UnitConversionMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnitConversionMappingMapper {
    public UnitConversionMappingDTO toDTO(UnitConversionMapping entity) {
        if (entity == null) return null;
        return new UnitConversionMappingDTO(
                entity.getId(),
                entity.getDataSource(),
                entity.getSourceUnit(),
                entity.getTargetUnit(),
                entity.getFormula(),
                entity.getDescription(),
                entity.getIsActivated()
        );
    }

    public UnitConversionMapping toEntity(UnitConversionMappingDTO dto) {
        if (dto == null) return null;
        return UnitConversionMapping.builder()
                .id(dto.getId())
                .dataSource(dto.getDataSource())
                .sourceUnit(dto.getSourceUnit())
                .targetUnit(dto.getTargetUnit())
                .formula(dto.getFormula())
                .description(dto.getDescription())
                .isActivated(dto.getIsActivated() != null ? dto.getIsActivated() : true)
                .build();
    }

    public void updateFromDto(UnitConversionMappingDTO dto, UnitConversionMapping entity) {
        if (dto == null || entity == null) return;

        if (dto.getDataSource() != null) entity.setDataSource(dto.getDataSource());
        if (dto.getSourceUnit() != null) entity.setSourceUnit(dto.getSourceUnit());
        if (dto.getTargetUnit() != null) entity.setTargetUnit(dto.getTargetUnit());
        if (dto.getFormula() != null) entity.setFormula(dto.getFormula());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getIsActivated() != null) entity.setIsActivated(dto.getIsActivated());
    }
}
