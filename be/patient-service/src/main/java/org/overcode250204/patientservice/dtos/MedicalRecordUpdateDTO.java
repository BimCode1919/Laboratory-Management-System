package org.overcode250204.patientservice.dtos;

import jakarta.validation.Valid;
import lombok.*;


import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MedicalRecordUpdateDTO {
    @Valid
    private PatientDTO patientDTO;
    private String notes;
    private List<String> clinicalNotes;
    private List<InterpretationUpdateDTO> interpretation;
}