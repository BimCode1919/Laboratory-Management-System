package org.overcode250204.instrumentservice.repository;

import org.overcode250204.instrumentservice.entity.InstalledReagent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstalledReagentRepository extends JpaRepository<InstalledReagent, UUID> {

    List<InstalledReagent> findByInstrumentIdAndInUseTrue(UUID instrumentId);


    Optional<InstalledReagent> findByInstrumentIdAndReagentNameAndLotNumber(
            UUID instrumentId, String reagentName, String lotNumber
    );

    List<InstalledReagent> findByInstrumentId(UUID instrumentId);

    Optional<InstalledReagent> findByInstrumentIdAndReagentId(UUID instrumentId, UUID reagentId);

    List<InstalledReagent> findByInstrumentIdAndReagentNameAndInUseTrueOrderByInstalledAtAsc(
            UUID instrumentId, String reagentName);


}