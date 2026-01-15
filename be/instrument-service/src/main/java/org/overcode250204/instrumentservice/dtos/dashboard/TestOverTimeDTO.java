package org.overcode250204.instrumentservice.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestOverTimeDTO {
    private String date;
    private long testCount;
}