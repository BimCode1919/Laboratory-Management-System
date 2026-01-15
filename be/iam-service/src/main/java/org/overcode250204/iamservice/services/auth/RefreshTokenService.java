package org.overcode250204.iamservice.services.auth;

import java.util.UUID;

public interface RefreshTokenService {
    String create(UUID userId, String deviceId, String rawRefreshTokenFromCognito);
    boolean validate(String token);
    UUID getUserId(String token);
    void revoke(String token);
}
