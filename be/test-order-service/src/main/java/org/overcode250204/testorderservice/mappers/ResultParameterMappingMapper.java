package org.overcode250204.testorderservice.mappers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.dtos.ResultParameterMappingDTO;
import org.overcode250204.testorderservice.models.entites.ResultParameterMapping;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResultParameterMappingMapper {
    public ResultParameterMappingDTO toDTO(ResultParameterMapping entity) {
        if (entity == null) return null;

        ResultParameterMappingDTO dto = new ResultParameterMappingDTO();
        dto.setId(entity.getId());
        dto.setExternalParamName(entity.getExternalParamName());
        dto.setInternalParamName(entity.getInternalParamName());
        dto.setDataSource(entity.getDataSource());
        dto.setIsActivated(entity.getIsActivated());
        return dto;
    }

    public ResultParameterMapping toEntity(ResultParameterMappingDTO dto) {
        if (dto == null) return null;

        ResultParameterMapping entity = new ResultParameterMapping();
        entity.setId(dto.getId());
        entity.setExternalParamName(dto.getExternalParamName());
        entity.setInternalParamName(dto.getInternalParamName());
        entity.setDataSource(dto.getDataSource());
        entity.setIsActivated(dto.getIsActivated());
        return entity;
    }

    public void updateEntity(ResultParameterMapping entity, ResultParameterMappingDTO dto) {
        if (entity == null || dto == null) return;

        entity.setExternalParamName(dto.getExternalParamName());
        entity.setInternalParamName(dto.getInternalParamName());
        entity.setDataSource(dto.getDataSource());
        entity.setIsActivated(dto.getIsActivated());
    }
}
