package org.overcode250204.iamservice.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatusCountDTO {
    private long active;
    private long inactive;
    private long locked;
}