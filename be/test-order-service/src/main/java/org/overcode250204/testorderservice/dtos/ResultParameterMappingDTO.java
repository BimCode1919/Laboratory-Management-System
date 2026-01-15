package org.overcode250204.testorderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.overcode250204.testorderservice.models.enums.Gender;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultParameterMappingDTO {
    private UUID id;
    private String externalParamName;
    private String internalParamName;
    private String dataSource;
    private Boolean isActivated;
}