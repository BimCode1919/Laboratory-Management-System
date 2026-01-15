package org.overcode250204.iamservice.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.iamservice.entities.Role;
import org.overcode250204.iamservice.repositories.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final RoleRepository roleRepository;

    @Transactional
    public void seedRoles() {
        Map<String, String> roles = Map.ofEntries(
                Map.entry("ADMIN", "Administrator who has rights to access all system features."),
                Map.entry("LAB_MANAGER", "Laboratory Manager responsible for managing lab, lab users, and services."),
                Map.entry("SERVICE_USER", "Authorized service operator who monitors and maintains system operations."),
                Map.entry("LAB_USER", "Lab User responsible for conducting tests, analyzing samples, and managing lab processes.")
        );

        roles.forEach((code, desc) -> roleRepository.findByCode(code)
                .ifPresentOrElse(
                        r -> {},
                        () -> roleRepository.save(
                                Role.builder()
                                        .code(code)
                                        .name(code.replace("_", " "))
                                        .description(desc)
                                        .build()
                        )
                )
        );

        log.info("Role seeding completed. Total roles: {}", roleRepository.count());
    }
}


