package org.overcode250204.iamservice.dto.role;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivilegeDTO {
    private Long id;
    private String code;
    private String name;
}
