package org.overcode250204.instrumentservice.dtos;

import lombok.*;
import org.overcode250204.instrumentservice.enums.InstrumentMode;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeChangeResultDTO {
    private UUID instrumentId;
    private InstrumentMode newMode;
    private InstrumentMode oldMode;
    private String reason;
    private UUID performedBy;
    private LocalDateTime changedAt;
}
