package org.overcode250204.iamservice.dto.role;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleListDTO {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private List<PrivilegeDTO> privileges;
}
