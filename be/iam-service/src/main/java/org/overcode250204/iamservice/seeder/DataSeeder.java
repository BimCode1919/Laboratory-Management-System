package org.overcode250204.iamservice.seeder;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PrivilegeSeeder privilegeSeeder;
    private final RoleSeeder roleSeeder;
    private final RolePrivilegeSeeder rolePrivilegeSeeder;
    private final AdminSeeder adminSeeder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data seeding...");
        privilegeSeeder.seedPrivileges();
        roleSeeder.seedRoles();
        rolePrivilegeSeeder.seedRolePrivileges();
        adminSeeder.seedAdmin();
        adminSeeder.seedManager();
        adminSeeder.seedServiceUser();
        adminSeeder.seedLabUser();
        log.info("Data seeding completed successfully.");
    }
}
