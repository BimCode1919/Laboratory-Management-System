package org.overcode250204.patientservice.repositories;

import org.overcode250204.patientservice.entities.TestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestRecordRepository extends JpaRepository<TestRecord, UUID> {
}
