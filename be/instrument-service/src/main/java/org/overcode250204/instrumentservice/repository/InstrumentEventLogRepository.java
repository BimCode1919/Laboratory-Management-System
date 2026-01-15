package org.overcode250204.instrumentservice.repository;

import org.overcode250204.instrumentservice.entity.InstrumentEventLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentEventLogRepository extends MongoRepository<InstrumentEventLog, String> {
}
