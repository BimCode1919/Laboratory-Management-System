package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.models.entites.PatientReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PatientReferenceRepository extends JpaRepository<PatientReference, UUID> {
    Optional<PatientReference> findByEmailOrPhoneNumber(String email, String phoneNumber);
    PatientReference findByPatientCode(UUID patientCode);

    @Query("SELECT p.gender FROM PatientReference p WHERE p.patientId = :patientId")
    String findGenderByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT p.gender FROM PatientReference p WHERE p.patientCode = :patientCode")
    String findGenderByPatientCode(UUID patientCode);
}