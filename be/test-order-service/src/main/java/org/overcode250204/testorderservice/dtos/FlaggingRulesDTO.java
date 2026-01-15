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
public class FlaggingRulesDTO {
    private UUID id;
    private String parameterName;
    private String unit;
    private Gender gender;
    private Double normalLow;
    private Double normalHigh;
    private String description;
    private Boolean isActivated;
}