package org.overcode250204.iamservice.repositories;

import org.overcode250204.iamservice.entities.Privilege;
import org.overcode250204.iamservice.entities.Role;
import org.overcode250204.iamservice.entities.RolePrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RolePrivilegeRepository extends JpaRepository<RolePrivilege, UUID> {
    boolean existsByRoleAndPrivilege(Role role, Privilege priv);

    @Query("""
        SELECT rp.privilege.name
        FROM RolePrivilege rp
        WHERE rp.role.name IN :roleName
""")
    List<String> findPrivilegeNamesByRoleName(@Param("roleNames") List<String> roleNames);

    @Query("""
        SELECT rp.privilege.code
        FROM RolePrivilege rp
        WHERE rp.role.code IN :roleCodes
""")
    List<String> findPrivilegeCodesByRoleCodes(@Param("roleCodes") List<String> roleCodes);

    void deleteByRoleId(UUID roleId);
}
