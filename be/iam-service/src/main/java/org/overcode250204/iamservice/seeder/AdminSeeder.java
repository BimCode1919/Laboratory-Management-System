package org.overcode250204.iamservice.seeder;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.iamservice.configs.aesgcm.SecurityProperties;
import org.overcode250204.iamservice.configs.cognito.CognitoProperties;
import org.overcode250204.iamservice.entities.Role;
import org.overcode250204.iamservice.entities.UserProfile;
import org.overcode250204.iamservice.entities.UserRole;
import org.overcode250204.iamservice.enums.Status;
import org.overcode250204.iamservice.repositories.RoleRepository;
import org.overcode250204.iamservice.repositories.UserRepository;
import org.overcode250204.iamservice.repositories.UserRoleRepository;
import org.overcode250204.iamservice.services.crypto.AESEncryptionService;
import org.overcode250204.iamservice.utils.HashUtil;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AESEncryptionService aesService;
    private final SecurityProperties securityProperties;
    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoProperties cognitoProperties;

    @Transactional
    public void seedAdmin(){
        String identifyNumber = "123123123123";
        String email = "admin@system.local";

        String hashedIdentifyNumber = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), identifyNumber);
        String hashedEmail = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), email);

        if(userRepository.existsByIdentifyNumberHash(hashedIdentifyNumber)) {
            log.info("Admin user already exists. Skipping seeding ...");
            return;
        }

        boolean existsInCognito = false;
        try {
            cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(email)
                    .build());
            existsInCognito = true;
            log.info("Admin user already exists in Cognito: {}", email);
        } catch (UserNotFoundException e) {
            log.info("Cognito user not found, will create new one.");
        }

        String cognitoSub;
        if (!existsInCognito) {
            AdminCreateUserResponse response = cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(email)
                    .temporaryPassword("Admin@123")
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("email_verified").value("true").build()
                    )
                    .messageAction("SUPPRESS")
                    .build());

            cognitoSub = response.user().attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow(() -> new RuntimeException("Cognito sub missing"));
        } else {
            cognitoSub = cognitoClient.listUsers(ListUsersRequest.builder()
                            .userPoolId(cognitoProperties.getUserPoolId())
                            .filter("email=\"" + email + "\"")
                            .build())
                    .users().getFirst()
                    .attributes().stream()
                    .filter(a -> a.name().equals("sub"))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow();
        }

        UserProfile admin = UserProfile.builder()
                .cognitoSub(cognitoSub)
                .identifyNumberEncrypt(aesService.encrypt(identifyNumber))
                .identifyNumberHash(hashedIdentifyNumber)
                .emailEncrypt(aesService.encrypt(email))
                .emailHash(hashedEmail)
                .fullname(aesService.encrypt("System Administrator"))
                .dob(LocalDate.of(1999,1,1))
                .age(99)
                .gender("OTHER")
                .address(aesService.encrypt("Viet Nam"))
                .phoneNumber(aesService.encrypt("0123456789"))
                .createdByAdmin(true)
                .status(Status.ACTIVE)
                .build();

        userRepository.save(admin);

        Role adminRole = roleRepository.findByCode("ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        UserRole link = UserRole.builder()
                .userProfile(admin)
                .role(adminRole)
                .assignedBy(admin.getId())
                .build();
        userRoleRepository.save(link);

        log.info("Admin account seeded successfully");
    }

    @Transactional
    public void seedManager(){
        String identifyNumber = "321321321321";
        String email = "manager@system.local";

        String hashedIdentifyNumber = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), identifyNumber);
        String hashedEmail = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), email);

        if(userRepository.existsByIdentifyNumberHash(hashedIdentifyNumber)) {
            log.info("Manager user already exists. Skipping seeding ...");
            return;
        }

        boolean existsInCognito = false;
        try {
            cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(email)
                    .build());
            existsInCognito = true;
            log.info("Manager user already exists in Cognito: {}", email);
        } catch (UserNotFoundException e) {
            log.info("Cognito user not found, will create new one.");
        }

        String cognitoSub;
        if (!existsInCognito) {
            AdminCreateUserResponse response = cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(email)
                    .temporaryPassword("Manager@123")
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("email_verified").value("true").build()
                    )
                    .messageAction("SUPPRESS")
                    .build());

            cognitoSub = response.user().attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow(() -> new RuntimeException("Cognito sub missing"));
        } else {
            cognitoSub = cognitoClient.listUsers(ListUsersRequest.builder()
                            .userPoolId(cognitoProperties.getUserPoolId())
                            .filter("email=\"" + email + "\"")
                            .build())
                    .users().getFirst()
                    .attributes().stream()
                    .filter(a -> a.name().equals("sub"))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow();
        }

        UserProfile admin = UserProfile.builder()
                .cognitoSub(cognitoSub)
                .identifyNumberEncrypt(aesService.encrypt(identifyNumber))
                .identifyNumberHash(hashedIdentifyNumber)
                .emailEncrypt(aesService.encrypt(email))
                .emailHash(hashedEmail)
                .fullname(aesService.encrypt("System Lab Manager"))
                .dob(LocalDate.of(1999,1,1))
                .age(99)
                .gender("OTHER")
                .address(aesService.encrypt("Viet Nam"))
                .phoneNumber(aesService.encrypt("0123456788"))
                .createdByAdmin(true)
                .status(Status.ACTIVE)
                .build();

        userRepository.save(admin);

        Role adminRole = roleRepository.findByCode("LAB_MANAGER")
                .orElseThrow(() -> new RuntimeException("Lab manager role not found"));

        UserRole link = UserRole.builder()
                .userProfile(admin)
                .role(adminRole)
                .assignedBy(admin.getId())
                .build();
        userRoleRepository.save(link);

        log.info("Lab Manager account seeded successfully");
    }

    @Transactional
    public void seedServiceUser(){
        String identifyNumber = "111111111111";
        String email = "service@system.local";

        String hashedIdentifyNumber = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), identifyNumber);
        String hashedEmail = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), email);

        if(userRepository.existsByIdentifyNumberHash(hashedIdentifyNumber)) {
            log.info("Service user already exists. Skipping seeding ...");
            return;
        }

        boolean existsInCognito = false;
        try {
            cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(email)
                    .build());
            existsInCognito = true;
            log.info("Service user already exists in Cognito: {}", email);
        } catch (UserNotFoundException e) {
            log.info("Cognito user not found, will create new one.");
        }

        String cognitoSub;
        if (!existsInCognito) {
            AdminCreateUserResponse response = cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(email)
                    .temporaryPassword("Service@123")
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("email_verified").value("true").build()
                    )
                    .messageAction("SUPPRESS")
                    .build());

            cognitoSub = response.user().attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow(() -> new RuntimeException("Cognito sub missing"));
        } else {
            cognitoSub = cognitoClient.listUsers(ListUsersRequest.builder()
                            .userPoolId(cognitoProperties.getUserPoolId())
                            .filter("email=\"" + email + "\"")
                            .build())
                    .users().getFirst()
                    .attributes().stream()
                    .filter(a -> a.name().equals("sub"))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow();
        }

        UserProfile admin = UserProfile.builder()
                .cognitoSub(cognitoSub)
                .identifyNumberEncrypt(aesService.encrypt(identifyNumber))
                .identifyNumberHash(hashedIdentifyNumber)
                .emailEncrypt(aesService.encrypt(email))
                .emailHash(hashedEmail)
                .fullname(aesService.encrypt("System Service User"))
                .dob(LocalDate.of(1999,1,1))
                .age(99)
                .gender("OTHER")
                .address(aesService.encrypt("Viet Nam"))
                .phoneNumber(aesService.encrypt("0123456787"))
                .createdByAdmin(true)
                .status(Status.ACTIVE)
                .build();

        userRepository.save(admin);

        Role adminRole = roleRepository.findByCode("SERVICE_USER")
                .orElseThrow(() -> new RuntimeException("Service User role not found"));

        UserRole link = UserRole.builder()
                .userProfile(admin)
                .role(adminRole)
                .assignedBy(admin.getId())
                .build();
        userRoleRepository.save(link);

        log.info("Service user account seeded successfully");
    }

    @Transactional
    public void seedLabUser(){
        String identifyNumber = "222222222222";
        String email = "labuser@system.local";

        String hashedIdentifyNumber = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), identifyNumber);
        String hashedEmail = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), email);

        if(userRepository.existsByIdentifyNumberHash(hashedIdentifyNumber)) {
            log.info("Lab user already exists. Skipping seeding ...");
            return;
        }

        boolean existsInCognito = false;
        try {
            cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(email)
                    .build());
            existsInCognito = true;
            log.info("Lab user already exists in Cognito: {}", email);
        } catch (UserNotFoundException e) {
            log.info("Cognito user not found, will create new one.");
        }

        String cognitoSub;
        if (!existsInCognito) {
            AdminCreateUserResponse response = cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(email)
                    .temporaryPassword("Labuser@123")
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("email_verified").value("true").build()
                    )
                    .messageAction("SUPPRESS")
                    .build());

            cognitoSub = response.user().attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow(() -> new RuntimeException("Cognito sub missing"));
        } else {
            cognitoSub = cognitoClient.listUsers(ListUsersRequest.builder()
                            .userPoolId(cognitoProperties.getUserPoolId())
                            .filter("email=\"" + email + "\"")
                            .build())
                    .users().getFirst()
                    .attributes().stream()
                    .filter(a -> a.name().equals("sub"))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow();
        }

        UserProfile admin = UserProfile.builder()
                .cognitoSub(cognitoSub)
                .identifyNumberEncrypt(aesService.encrypt(identifyNumber))
                .identifyNumberHash(hashedIdentifyNumber)
                .emailEncrypt(aesService.encrypt(email))
                .emailHash(hashedEmail)
                .fullname(aesService.encrypt("System Lab User"))
                .dob(LocalDate.of(1999,1,1))
                .age(99)
                .gender("OTHER")
                .address(aesService.encrypt("Viet Nam"))
                .phoneNumber(aesService.encrypt("0123456786"))
                .createdByAdmin(true)
                .status(Status.ACTIVE)
                .build();

        userRepository.save(admin);

        Role adminRole = roleRepository.findByCode("LAB_USER")
                .orElseThrow(() -> new RuntimeException("Lab user role not found"));

        UserRole link = UserRole.builder()
                .userProfile(admin)
                .role(adminRole)
                .assignedBy(admin.getId())
                .build();
        userRoleRepository.save(link);

        log.info("Lab user account seeded successfully");
    }


}
