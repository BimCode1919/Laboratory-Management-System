package org.overcode250204.iamservice.repositories;

import org.overcode250204.iamservice.dto.dashboard.RoleUserCountDTO;
import org.overcode250204.iamservice.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findAllByRoleId(UUID roleId);

    @Query("""
    SELECT new org.overcode250204.iamservice.dto.dashboard.RoleUserCountDTO(
        r.code,
        COUNT(ur.id)
    )
    FROM Role r
    LEFT JOIN UserRole ur ON ur.role.id = r.id
    GROUP BY r.id, r.code
""")
    List<RoleUserCountDTO> countUsersByRole();

}
