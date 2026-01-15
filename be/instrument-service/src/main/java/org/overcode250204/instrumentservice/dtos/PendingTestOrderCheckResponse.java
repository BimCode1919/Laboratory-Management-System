package org.overcode250204.instrumentservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingTestOrderCheckResponse {
    private boolean exists;
    private boolean isPending;
    private boolean instrumentExists;
    private boolean hasMatchingConfig;
    private String testType;
}
