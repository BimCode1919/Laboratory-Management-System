package org.overcode250204.iamservice.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleUpdateDTO {
    @NotBlank(message = "Role name is required")
    private String name;

    private String description;

    private List<Long> privilegeIds;
}
