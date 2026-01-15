package org.overcode250204.monitoringservice.repositories;

import org.overcode250204.monitoringservice.entities.HL7Backup;
import org.overcode250204.monitoringservice.enums.RawTestResultStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HL7BackupRepo extends MongoRepository<HL7Backup, String> {

    Optional<HL7Backup> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    List<HL7Backup> findByRunId(String runId);

    Page<HL7Backup> findByInstrumentId(String instrumentId, Pageable pageable);

    Page<HL7Backup> findByStatus(String status, Pageable pageable);
}
