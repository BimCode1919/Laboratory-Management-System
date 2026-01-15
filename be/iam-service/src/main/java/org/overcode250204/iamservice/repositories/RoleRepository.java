package org.overcode250204.iamservice.repositories;

import jakarta.validation.constraints.NotBlank;
import org.overcode250204.iamservice.entities.Role;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
   Optional<Role> findByCode(String roleCode);

    boolean existsByCode(@NotBlank(message = "Role code is required") String code);

    List<Role> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code, Sort sort);
}
