package org.overcode250204.iamservice.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.iamservice.entities.Privilege;
import org.overcode250204.iamservice.repositories.PrivilegeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivilegeSeeder {

    private final PrivilegeRepository privilegeRepository;

    @Transactional
    public void seedPrivileges() {
        Map<String, String> privileges = Map.ofEntries(
                // --- Test Order ---
                Map.entry("TEST_ORDER_READ", "Allows viewing patient test orders and their results."),
                Map.entry("TEST_ORDER_CREATE", "Allows creating new patient test orders."),
                Map.entry("TEST_ORDER_UPDATE", "Allows modifying existing patient test orders."),
                Map.entry("TEST_ORDER_DELETE", "Allows deleting patient test orders."),
                Map.entry("TEST_ORDER_REVIEW", "Allows reviewing and modifying test results of test orders."),

                // --- Comment ---
                Map.entry("COMMENT_CREATE", "Allows adding new comments for test results."),
                Map.entry("COMMENT_UPDATE", "Allows modifying existing comments."),
                Map.entry("COMMENT_DELETE", "Allows deleting comments."),

                // --- Configuration ---
                Map.entry("CONFIG_READ", "Allows viewing, adding, modifying, and deleting configurations."),
                Map.entry("CONFIG_CREATE", "Allows creating new configuration entries."),
                Map.entry("CONFIG_UPDATE", "Allows updating existing configurations."),
                Map.entry("CONFIG_DELETE", "Allows deleting configurations."),

                // --- User ---
                Map.entry("USER_READ", "Allows viewing all user profiles."),
                Map.entry("USER_CREATE", "Allows creating new users."),
                Map.entry("USER_UPDATE", "Allows updating existing users."),
                Map.entry("USER_DELETE", "Allows deleting users."),
                Map.entry("USER_LOCK", "Allows locking and unlocking user accounts."),

                // --- Role ---
                Map.entry("ROLE_VIEW", "Allows viewing all role privileges."),
                Map.entry("ROLE_CREATE", "Allows creating new custom roles."),
                Map.entry("ROLE_UPDATE", "Allows modifying privileges of existing roles."),
                Map.entry("ROLE_DELETE", "Allows deleting custom roles."),

                // --- Event Log ---
                Map.entry("EVENTLOG_READ", "Allows viewing event logs."),

                // --- Reagents ---
                Map.entry("REAGENT_CREATE", "Allows adding new reagents."),
                Map.entry("REAGENT_UPDATE", "Allows modifying reagent information."),
                Map.entry("REAGENT_DELETE", "Allows deleting reagents."),

                // --- Instrument ---
                Map.entry("INSTRUMENT_CREATE", "Allows adding new instruments into system management."),
                Map.entry("INSTRUMENT_READ", "Allows viewing all instruments and checking their status."),
                Map.entry("INSTRUMENT_UPDATE", "Allows activating or deactivating instruments."),

                // --- Execution ---
                Map.entry("TEST_EXECUTE", "Allows executing blood testing processes."),

                // --- Read_only ---
                Map.entry("READ_ONLY", "Allows only patient can read.")
        );

        privileges.forEach((code, desc) -> privilegeRepository.findByCode(code)
                .ifPresentOrElse(
                        p -> {},
                        () -> privilegeRepository.save(
                                Privilege.builder()
                                        .code(code)
                                        .name(code.replace("_", " "))
                                        .description(desc)
                                        .build()
                        )
                )
        );

        log.info("Privilege seeding completed. Total privileges: {}", privilegeRepository.count());
    }
}
