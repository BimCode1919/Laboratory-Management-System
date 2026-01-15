package org.overcode250204.instrumentservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationSyncLogRepository extends MongoRepository<ConfigurationSyncLogRepository, String> {
}
