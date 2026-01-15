package org.overcode250204.iamservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.iamservice.dto.user.UserProfileDTO;
import org.overcode250204.iamservice.exceptions.ErrorCode;
import org.overcode250204.iamservice.exceptions.IamServiceException;
import org.overcode250204.iamservice.services.user.UserProfileService;
import org.overcode250204.iamservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Value("${spring.application.name}")
    private String serviceName;

    @PostMapping()
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserProfileDTO dto,
            @RequestParam String roleCode
            ){
        try {
            String userId = AuthUtils.getCurrentUser().getPrincipal().toString();
            UUID createByUserIdUUID = UUID.fromString(userId);
            return ResponseEntity.ok(BaseResponse.success(serviceName, userProfileService.createUser(dto, roleCode, createByUserIdUUID)));
        } catch (Exception e){
            throw new IamServiceException(ErrorCode.UUID_PATH_STRING_FAILED);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserById() {
        return ResponseEntity.ok(BaseResponse.success(serviceName, userProfileService.getUserById(UUID.fromString(AuthUtils.getCurrentUser().getPrincipal().toString()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(BaseResponse.success(serviceName, userProfileService.getUserById(id)));
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(BaseResponse.success(serviceName, userProfileService.getAllUsers()));
    }

        @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @Valid @RequestBody UserProfileDTO dto) {
        try {
            String userId = AuthUtils.getCurrentUser().getPrincipal().toString();
            UUID updatedByUserIdUUID = UUID.fromString(userId);

            return ResponseEntity.ok(
                    BaseResponse.success(
                            serviceName,
                            userProfileService.updateUser(id, dto, updatedByUserIdUUID)
                    )
            );
        } catch (Exception e){
            throw new IamServiceException(ErrorCode.UUID_PATH_STRING_FAILED);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<?> disableUser(@PathVariable UUID id) {
        String disabledBy =  AuthUtils.getCurrentUser().getPrincipal().toString();
        return ResponseEntity.ok(BaseResponse.success(serviceName, userProfileService.disableUser(id, disabledBy)));
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<?> restoreUser(@PathVariable UUID id) {
        String enableBy =  AuthUtils.getCurrentUser().getPrincipal().toString();
        return ResponseEntity.ok(BaseResponse.success(serviceName, userProfileService.enableUser(id, enableBy)));
    }
}
