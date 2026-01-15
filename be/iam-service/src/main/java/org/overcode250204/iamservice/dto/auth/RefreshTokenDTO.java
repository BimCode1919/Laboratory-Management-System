package org.overcode250204.iamservice.dto.auth;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class RefreshTokenDTO {
    private String sub;
    private String deviceId;
}
