package org.overcode250204.patientservice.services;

import org.overcode250204.patientservice.entities.MedicalRecord;

public interface MedicalRecordSyncService {
    void indexMedicalRecord(MedicalRecord record);
}
