package org.overcode250204.instrumentservice.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InstrumentTestCountDTO {
    private UUID instrumentId;
    private String instrumentName;
    private long testCount;
}
