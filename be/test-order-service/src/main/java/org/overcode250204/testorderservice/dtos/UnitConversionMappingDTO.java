package org.overcode250204.testorderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitConversionMappingDTO {
    private UUID id;
    private String dataSource;
    private String sourceUnit;
    private String targetUnit;
    private String formula;
    private String description;
    private Boolean isActivated;
}