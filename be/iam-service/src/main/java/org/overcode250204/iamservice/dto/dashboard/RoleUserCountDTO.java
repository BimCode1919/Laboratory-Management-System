package org.overcode250204.iamservice.dto.dashboard;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class RoleUserCountDTO {
    private String roleCode;
    private long userCount;
}
