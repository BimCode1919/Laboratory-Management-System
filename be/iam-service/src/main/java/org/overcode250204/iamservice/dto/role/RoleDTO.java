package org.overcode250204.iamservice.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {
    @NotBlank(message = "Role code is required")
    private String code;

    @NotBlank(message = "Role name is required")
    private String name;

    private String description;

    private List<Long> privilegeIds;
}
