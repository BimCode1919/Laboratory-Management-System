package org.overcode250204.iamservice.repositories;

import org.overcode250204.iamservice.entities.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    Optional<Privilege> findByCode(String code);
}
