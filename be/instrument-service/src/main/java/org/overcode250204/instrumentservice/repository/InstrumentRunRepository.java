package org.overcode250204.instrumentservice.repository;

import org.overcode250204.instrumentservice.entity.InstrumentRun;
import org.overcode250204.instrumentservice.enums.InstrumentRunStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstrumentRunRepository extends MongoRepository<InstrumentRun, String> {

    boolean existsByInstrumentIdAndStatus(UUID instrumentId, InstrumentRunStatus status);

    Optional<InstrumentRun> findByRunId(UUID runId);

    List<InstrumentRun> findByInstrumentId(UUID instrumentId);

}
