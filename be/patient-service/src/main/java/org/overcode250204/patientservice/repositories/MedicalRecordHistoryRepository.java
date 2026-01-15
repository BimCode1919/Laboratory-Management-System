
package org.overcode250204.patientservice.repositories;

import org.overcode250204.patientservice.entities.MedicalRecordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

public interface MedicalRecordHistoryRepository extends JpaRepository<MedicalRecordHistory, UUID> {
    long countByPatient_PatientCode(UUID patientCode);
}
