package org.overcode250204.patientservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.overcode250204.patientservice.enums.TestRecordStatus;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InterpretationUpdateDTO {

    private UUID testRecordId;

    private String interpretation;

    private TestRecordStatus status;

}
