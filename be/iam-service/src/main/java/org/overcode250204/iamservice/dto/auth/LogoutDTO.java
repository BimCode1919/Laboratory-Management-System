package org.overcode250204.iamservice.dto.auth;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class LogoutDTO {
    private UUID userId;
    private String deviceId;
}
