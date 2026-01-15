package org.overcode250204.instrumentservice.service.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import org.overcode250204.instrumentservice.dtos.InstallReagentCommand;
import org.overcode250204.instrumentservice.dtos.ReagentDTO;
import org.overcode250204.instrumentservice.dtos.UninstallReagentCommand;
import org.overcode250204.instrumentservice.dtos.UpdateReagentInUseCommand;

import java.util.List;
import java.util.UUID;

public interface ReagentService {
    boolean hasSufficientReagent(UUID instrumentId, String testType, int samplesNeeded);

    void consumeReagent(UUID instrumentId, String testType, int samplesUsed);

    JsonNode snapshotReagent(UUID instrumentId);

    List<ReagentDTO> getInstalledReagents(UUID instrumentId);

    JsonNode requestInstallReagent(UUID instrumentId, InstallReagentCommand cmd);

    void requestUninstallReagent(UUID instrumentId, UninstallReagentCommand cmd);

    JsonNode requestSyncReagent(UUID instrumentId);

    // New methods for inUse flag
    ReagentDTO getReagent(UUID instrumentId, UUID reagentId);

    ReagentDTO updateReagentInUse(UUID instrumentId, UUID reagentId, UpdateReagentInUseCommand cmd);
}