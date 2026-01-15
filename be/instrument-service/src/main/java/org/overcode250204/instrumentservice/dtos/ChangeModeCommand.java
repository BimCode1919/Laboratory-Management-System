package org.overcode250204.instrumentservice.dtos;

import lombok.Getter;
import lombok.Setter;
import org.overcode250204.instrumentservice.enums.InstrumentMode;

import java.util.UUID;

@Getter
@Setter
public class ChangeModeCommand {
    private InstrumentMode mode;
    private UUID userId;
    private String reason;
}
