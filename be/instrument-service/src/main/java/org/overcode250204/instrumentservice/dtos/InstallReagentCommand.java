package org.overcode250204.instrumentservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class InstallReagentCommand {
    private UUID reagentId;
    private double quantity;
}
