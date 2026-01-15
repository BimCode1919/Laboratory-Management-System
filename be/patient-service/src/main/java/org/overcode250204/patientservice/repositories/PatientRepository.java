package org.overcode250204.patientservice.repositories;

import org.overcode250204.patientservice.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    @Query("""
    SELECT p
    FROM patient p
    WHERE p.dateOfBirth = :dateOfBirth AND (p.phoneHash = :phoneHash OR p.emailHash = :emailHash)
""")
    Optional<Patient> findByDateOfBirthAndPhoneOrEmail(@Param("dateOfBirth") LocalDate dateOfBirth, @Param("phoneHash") String phoneHash, @Param("emailHash") String emailHash);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<Patient> findByPatientCode(UUID patientCode);

    Patient findByEmailHash(String emailHash);

    Patient findByCognitoSub(String cognitoSub);
}
