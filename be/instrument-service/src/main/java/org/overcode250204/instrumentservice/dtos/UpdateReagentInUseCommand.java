package org.overcode250204.instrumentservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReagentInUseCommand {
    private Boolean inUse;
}

