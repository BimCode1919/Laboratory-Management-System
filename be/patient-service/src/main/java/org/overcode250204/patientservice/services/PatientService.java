package org.overcode250204.patientservice.services;

import org.overcode250204.patientservice.dtos.PatientDTO;

import java.util.UUID;

public interface PatientService {
    PatientDTO updatePatient(UUID patientCode, PatientDTO patientDTO, String updatedBy);
}
