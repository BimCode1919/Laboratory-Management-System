package org.overcode250204.iamservice.services.user;

import org.overcode250204.iamservice.dto.user.UserProfileDTO;

import java.util.List;
import java.util.UUID;

public interface UserProfileService {
    String createUser(UserProfileDTO dto, String roleCode, UUID createBy);
    UserProfileDTO getUserById(UUID sub);
    List<UserProfileDTO> getAllUsers();
    String updateUser(UUID userId, UserProfileDTO dto, UUID updatedBy);
    String disableUser(UUID id, String disabledBy);
    String enableUser(UUID id, String enabledBy);

    UserProfileDTO getUserById(String id);
}
