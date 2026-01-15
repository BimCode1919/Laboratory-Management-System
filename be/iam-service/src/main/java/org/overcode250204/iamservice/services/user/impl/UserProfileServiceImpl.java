package org.overcode250204.iamservice.services.user.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.DuplicateResourceException;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.overcode250204.iamservice.configs.aesgcm.SecurityProperties;
import org.overcode250204.iamservice.configs.cognito.CognitoProperties;
import org.overcode250204.iamservice.dto.user.UserProfileDTO;
import org.overcode250204.iamservice.entities.OutboxEvent;
import org.overcode250204.iamservice.entities.Role;
import org.overcode250204.iamservice.entities.UserProfile;
import org.overcode250204.iamservice.entities.UserRole;
import org.overcode250204.iamservice.enums.Status;
import org.overcode250204.iamservice.exceptions.ErrorCode;
import org.overcode250204.iamservice.exceptions.IamServiceException;
import org.overcode250204.iamservice.mappers.UserProfileMapper;
import org.overcode250204.iamservice.repositories.OutboxEventRepository;
import org.overcode250204.iamservice.repositories.RoleRepository;
import org.overcode250204.iamservice.repositories.UserRepository;
import org.overcode250204.iamservice.repositories.UserRoleRepository;
import org.overcode250204.iamservice.services.auth.CognitoService;
import org.overcode250204.iamservice.services.crypto.AESEncryptionService;
import org.overcode250204.iamservice.services.user.UserProfileService;
import org.overcode250204.iamservice.utils.AuditLogUtils;
import org.overcode250204.iamservice.utils.HashUtil;
import org.overcode250204.iamservice.utils.MaskData;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {
    private final UserRepository userRepository;
    private final SecurityProperties securityProperties;
    private final AESEncryptionService aesService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoProperties cognitoProperties;
    private final UserProfileMapper mapper;
    private final CognitoService cognitoService;
    private final AuditLogUtils auditLogUtils;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    @Override
    public String createUser(UserProfileDTO dto, String roleCode, UUID createBy){
        String identifyNumberHash = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), dto.getIdentifyNumber());
        String emailHash = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), dto.getEmail());

        if(userRepository.existsByIdentifyNumberHash(identifyNumberHash)){
            throw new DuplicateResourceException("Identify number already exists.");
        }

        if(userRepository.existsByEmailHash(emailHash)){
            throw new DuplicateResourceException("Email already exists.");
        }

        String email = dto.getEmail();
        String cognitoSub;
        try{
            AdminCreateUserResponse response = cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(email)
                    .temporaryPassword(dto.getPassword())
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("email_verified").value("true").build()
                    )
                    .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .forceAliasCreation(Boolean.FALSE)
                    .build()
            );

            cognitoSub = response.user().attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElseThrow(() -> new RuntimeException("Cognito sub missing"));

            cognitoClient.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                    .userPoolId(cognitoProperties.getUserPoolId())
                    .username(email)
                    .groupName(roleCode)
                    .build());

            if ("PATIENT".equalsIgnoreCase(roleCode)) {
                cognitoClient.adminSetUserPassword(
                        AdminSetUserPasswordRequest.builder()
                                .userPoolId(cognitoProperties.getUserPoolId())
                                .username(email)
                                .password(dto.getPassword())
                                .permanent(true)
                                .build()
                );
            }
        } catch (UsernameExistsException e){
            throw new DuplicateResourceException("Email already exists in Cognito.");
        } catch (Exception e) {
            throw new IamServiceException(ErrorCode.EMAIL_EXISTS);
        }

        UserProfile.UserProfileBuilder user = UserProfile.builder()
                .cognitoSub(cognitoSub)
                .identifyNumberEncrypt(aesService.encrypt(dto.getIdentifyNumber()))
                .identifyNumberHash(identifyNumberHash)
                .emailHash(emailHash)
                .emailEncrypt(aesService.encrypt(dto.getEmail()))
                .phoneNumber(aesService.encrypt(dto.getPhoneNumber()))
                .fullname(aesService.encrypt(dto.getFullName()))
                .dob(dto.getDob())
                .gender(dto.getGender())
                .address(aesService.encrypt(dto.getAddress()))
                .createdByAdmin(true)
                .status(Status.ACTIVE)
                .createdBy(createBy)
                .age(Period.between(dto.getDob(), LocalDate.now()).getYears());

        if ("PATIENT".equalsIgnoreCase(roleCode)) {
            user.password(aesService.encrypt(dto.getPassword()));
        }

        UserProfile userSetRole = userRepository.save(user.build());

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        UserRole link = UserRole.builder()
                .userProfile(userSetRole)
                .role(role)
                .assignedBy(createBy)
                .build();
        userRoleRepository.save(link);
        OutboxEvent outboxEvent = new OutboxEvent();
        try {
            outboxEvent.setPayload(objectMapper.writeValueAsString(userSetRole));
            outboxEvent.setCreatedAt(Instant.now());
            outboxEvent.setEventType("IAM_USER_PATIENT_CREATED");
            outboxEvent.setAggregateType("USER_PATIENT_CREATE");
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new IamServiceException(ErrorCode.FAIL_TO_SAVE_OUTBOX);
        }
        auditLogUtils.createAuditOutboxEvent(
                "USER_CREATE",
                userSetRole.getId().toString(),
                "IAM_USER_CREATED",
                createBy.toString(),
                Map.of(
                        "userId", userSetRole.getId().toString(),
                        "role", roleCode,
                        "identifyNumberHash", identifyNumberHash
                )
        );

        return "Create user successfully!";
    }

    @Override
    public UserProfileDTO getUserById(UUID sub){
        UserProfile user = userRepository.findByCognitoSub(sub.toString())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapper.toDto(user);
    }
    @Override
    public List<UserProfileDTO> getAllUsers() {
        return userRepository.findAllWithRoles().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public String updateUser(UUID userId, UserProfileDTO dto, UUID updatedBy) {

        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        String identifyNumberHash = HashUtil.hmacSha256Base64(
                securityProperties.getBase64Pepper(), dto.getIdentifyNumber());
        String emailHash = HashUtil.hmacSha256Base64(
                securityProperties.getBase64Pepper(), dto.getEmail());

        userRepository.findByEmailHash(emailHash)
                .filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> { throw new DuplicateResourceException("Email already exists."); });

        userRepository.findByIdentifyNumberHash(identifyNumberHash)
                .filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> { throw new DuplicateResourceException("Identify number already exists."); });

        List<String> changes = new ArrayList<>();

        if (dto.getIdentifyNumber() != null) {
            user.setIdentifyNumberEncrypt(aesService.encrypt(dto.getIdentifyNumber()));
            user.setIdentifyNumberHash(identifyNumberHash);
            changes.add(String.format("Field 'identifyNumber' was updated (from: '%s' to: '%s')",
                    MaskData.maskData(aesService.decrypt(user.getIdentifyNumberEncrypt()), true), MaskData.maskData(dto.getIdentifyNumber(), true)));
        }

        if (dto.getEmail() != null) {
            user.setEmailEncrypt(aesService.encrypt(dto.getEmail()));
            user.setEmailHash(emailHash);
            changes.add(String.format("Field 'email' was updated (to: '%s')", MaskData.maskData(dto.getEmail(), true)));
        }

        auditLogUtils.updateEncryptedField(
                user::getFullname,
                dto.getFullName(),
                user::setFullname,
                "fullName",
                changes,
                false
        );
        auditLogUtils.updateEncryptedField(
                user::getPhoneNumber,
                dto.getPhoneNumber(),
                user::setPhoneNumber,
                "phone",
                changes,
                true
        );
        auditLogUtils.updateEncryptedField(
                user::getAddress,
                dto.getAddress(),
                user::setAddress,
                "address",
                changes,
                false
        );

        LocalDate newDob = dto.getDob();
        if (newDob != null && !Objects.equals(user.getDob(), newDob)) {
            user.setDob(newDob);
            int newAge = Period.between(newDob, LocalDate.now()).getYears();
            user.setAge(newAge);
            changes.add(String.format("Field 'dob' was updated (to: '%s')", newDob));
            changes.add(String.format("Field 'age' was updated (to: '%d')", newAge));
        }

        if (dto.getGender() != null && !Objects.equals(user.getGender(), dto.getGender())) {
            user.setGender(dto.getGender());
            changes.add("Field 'gender' was updated. From " + user.getGender() + " to " + dto.getGender());
        }


//        user.setFullname(aesService.encrypt(dto.getFullName()));
//        user.setDob(dto.getDob());
//        user.setAge(Period.between(dto.getDob(), LocalDate.now()).getYears());
//        user.setGender(dto.getGender());
//        user.setAddress(aesService.encrypt(dto.getAddress()));
//        user.setPhoneNumber(aesService.encrypt(dto.getPhoneNumber()));
        user.setUpdatedBy(updatedBy);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        if (!changes.isEmpty()) {
            auditLogUtils.createAuditOutboxEvent(
                    "USER_UPDATE",
                    user.getId().toString(),
                    "IAM_USER_UPDATED",
                    updatedBy.toString(),
                    Map.of("changes", changes)
            );
        }
        return "User information updated successfully!";
    }

    @Override
    @Transactional
    public String disableUser(UUID id, String disabledBy) {
        UserProfile user = userRepository.findById(id)
                .orElseThrow(() -> new IamServiceException(ErrorCode.USER_NOT_FOUND_WITH_ID));

        if (user.getStatus() == Status.INACTIVE) {
            throw new IamServiceException(ErrorCode.USER_ALREADY_DISABLE);
        }
        String decryptEmail = aesService.decrypt(user.getEmailEncrypt());

        cognitoService.disableUser(decryptEmail);
        user.setStatus(Status.INACTIVE);
        userRepository.save(user);
        auditLogUtils.createAuditOutboxEvent(
                "DISABLE_USER",
                user.getId().toString(),
                "IAM_DISABLE_USER",
                disabledBy,
                Map.of("email", decryptEmail)
                );

        return "Disable user successful!";
    }

    @Override
    @Transactional
    public String enableUser(UUID id, String enabledBy) {
        UserProfile user = userRepository.findById(id)
                .orElseThrow(() -> new IamServiceException(ErrorCode.USER_NOT_FOUND_WITH_ID));

        if (user.getStatus() == Status.ACTIVE) {
            throw new IamServiceException(ErrorCode.USER_ALREADY_ACTIVE);
        }
        String decryptEmail = aesService.decrypt(user.getEmailEncrypt());
        cognitoService.enableUser(decryptEmail);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        auditLogUtils.createAuditOutboxEvent(
                "ENABLE_USER",
                user.getId().toString(),
                "IAM_ENABLE_USER",
                enabledBy,
                Map.of("email", decryptEmail)
        );
        return "Enable user successful!";
    }

    @Override
    public UserProfileDTO getUserById(String id) {
        UserProfile user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapper.toDto(user);
    }
}
