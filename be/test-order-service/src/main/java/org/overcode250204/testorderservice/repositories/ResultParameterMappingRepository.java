package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.models.entites.ResultParameterMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResultParameterMappingRepository extends JpaRepository<ResultParameterMapping, UUID> {
    Optional<ResultParameterMapping> findByExternalParamNameAndIsActivatedTrue(String externalParamName);
}
