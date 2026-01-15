package org.overcode250204.testorderservice.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestResultItem {
    private String parameter; // e.g., WBC, RBC
    private String value;
    private String unit;
    private String flag; // temporary flag if instrument service sends, wont matter much
}