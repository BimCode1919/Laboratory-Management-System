package org.overcode250204.iamservice.mappers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.iamservice.dto.role.RoleDTO;
import org.overcode250204.iamservice.dto.user.UserProfileDTO;
import org.overcode250204.iamservice.entities.Role;
import org.overcode250204.iamservice.entities.UserProfile;
import org.overcode250204.iamservice.entities.UserRole;
import org.overcode250204.iamservice.services.crypto.AESEncryptionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserProfileMapper {
    private final AESEncryptionService aesService;

    public UserProfileDTO toDto(UserProfile entity) {
        List<String> roleNames = entity.getUserRoles() != null
                ? entity.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .map(Role::getName)
                .toList()
                : List.of();

        return UserProfileDTO.builder()
                .id(entity.getId())
                .identifyNumber(aesService.decrypt(entity.getIdentifyNumberEncrypt()))
                .email(aesService.decrypt(entity.getEmailEncrypt()))
                .fullName(aesService.decrypt(entity.getFullname()))
                .dob(entity.getDob())
                .age(entity.getAge())
                .phoneNumber(aesService.decrypt(entity.getPhoneNumber()))
                .gender(entity.getGender())
                .address(aesService.decrypt(entity.getAddress()))
                .status(entity.getStatus().name())
                .roles(roleNames)
                .build();
    }
}
