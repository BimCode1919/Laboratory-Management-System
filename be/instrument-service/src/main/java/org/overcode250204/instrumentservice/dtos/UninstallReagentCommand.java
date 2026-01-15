package org.overcode250204.instrumentservice.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class UninstallReagentCommand {
    private UUID reagentId;
    private Double quantityRemaining;
}