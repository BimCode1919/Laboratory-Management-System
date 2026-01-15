package org.overcode250204.iamservice.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.iamservice.entities.Privilege;
import org.overcode250204.iamservice.entities.Role;
import org.overcode250204.iamservice.entities.RolePrivilege;
import org.overcode250204.iamservice.repositories.PrivilegeRepository;
import org.overcode250204.iamservice.repositories.RolePrivilegeRepository;
import org.overcode250204.iamservice.repositories.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RolePrivilegeSeeder {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final RolePrivilegeRepository rolePrivilegeRepository;

    @Transactional
    public void seedRolePrivileges() {
        Map<String, List<String>> mapping = Map.ofEntries(
                // --- ADMIN: has all privileges ---
                Map.entry("ADMIN", privilegeRepository.findAll()
                        .stream()
                        .map(Privilege::getCode)
                        .toList()),

                // --- LAB_MANAGER ---
                Map.entry("LAB_MANAGER", List.of(
                        "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE", "USER_LOCK",
                        "ROLE_VIEW", "ROLE_CREATE", "ROLE_UPDATE", "ROLE_DELETE",
                        "CONFIG_READ", "CONFIG_CREATE", "CONFIG_UPDATE", "CONFIG_DELETE",
                        "EVENTLOG_READ",
                        "REAGENT_CREATE", "REAGENT_UPDATE", "REAGENT_DELETE",
                        "INSTRUMENT_CREATE", "INSTRUMENT_READ", "INSTRUMENT_UPDATE",
                        "TEST_ORDER_READ"
                )),

                // --- SERVICE ---
                Map.entry("SERVICE_USER", List.of(
                        "INSTRUMENT_READ", "INSTRUMENT_UPDATE",
                        "REAGENT_CREATE", "REAGENT_UPDATE",
                        "EVENTLOG_READ"
                )),

                // --- LAB_USER ---
                Map.entry("LAB_USER", List.of(
                        "TEST_ORDER_CREATE", "TEST_ORDER_READ", "TEST_ORDER_UPDATE",
                        "TEST_ORDER_DELETE", "TEST_ORDER_REVIEW",
                        "COMMENT_CREATE", "COMMENT_UPDATE", "COMMENT_DELETE",
                        "REAGENT_CREATE", "REAGENT_UPDATE",
                        "INSTRUMENT_READ", "TEST_EXECUTE"
                ))
        );

        mapping.forEach((roleCode, privilegeCodes) -> {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));

            privilegeCodes.forEach(code -> {
                Privilege priv = privilegeRepository.findByCode(code)
                        .orElseThrow(() -> new RuntimeException("Privilege not found: " + code));

                if (!rolePrivilegeRepository.existsByRoleAndPrivilege(role, priv)) {
                    rolePrivilegeRepository.save(
                            RolePrivilege.builder()
                                    .role(role)
                                    .privilege(priv)
                                    .build()
                    );
                }
            });
        });

        log.info("Role–Privilege mapping seeded successfully.");
    }
}


