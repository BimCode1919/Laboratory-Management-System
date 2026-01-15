package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.models.entites.TestResultRaw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestResultRawRepository extends JpaRepository<TestResultRaw, UUID> {
    List<TestResultRaw> findAllByRunIdAndIsProcessedFalse(UUID runId);

    List<TestResultRaw> findByRunIdInAndIsProcessedFalse(List<UUID> runIds);
}