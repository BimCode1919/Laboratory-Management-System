package org.overcode250204.patientservice.services;

import org.overcode250204.patientservice.dtos.MedicalRecordCreateDTO;
import org.overcode250204.patientservice.dtos.MedicalRecordDTO;
import org.overcode250204.patientservice.dtos.MedicalRecordUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MedicalRecordService {
    MedicalRecordDTO addRecord(MedicalRecordCreateDTO dto, String createdByLabUser);
    MedicalRecordDTO updateRecord(UUID recordId, MedicalRecordUpdateDTO medicalRecordUpdateDTO, String updatedBy);
    String deleteRecord(UUID recordId, String deletedByLabUser);
    Page<MedicalRecordDTO> getAllRecords(Pageable pageable);
    MedicalRecordDTO getRecordById(UUID recordId, String accessedBy);

}