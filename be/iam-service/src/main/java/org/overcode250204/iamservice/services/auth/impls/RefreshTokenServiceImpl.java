package org.overcode250204.iamservice.services.auth.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.iamservice.configs.aesgcm.SecurityProperties;
import org.overcode250204.iamservice.entities.UserToken;
import org.overcode250204.iamservice.repositories.UserTokenRepository;
import org.overcode250204.iamservice.services.auth.RefreshTokenService;
import org.overcode250204.iamservice.services.auth.TokenRevokeService;
import org.overcode250204.iamservice.services.crypto.AESEncryptionService;
import org.overcode250204.iamservice.utils.HashUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final UserTokenRepository repo;
    private final AESEncryptionService aes;
    private final TokenRevokeService revokeService;
    private final RedisTemplate<String, String> redisTemplate;
    private final SecurityProperties securityProperties;

    @Value("${REDIS_TOKEN_PREFIX:refresh-token:}")
    private String redisPrefix;

    @Value("${REDIS_TOKEN_TTL:2592000}")
    private long redisTtlSeconds;

    public RefreshTokenServiceImpl(
            UserTokenRepository repo,
            AESEncryptionService aes,
            TokenRevokeService revokeService,
            SecurityProperties securityProperties,
            @Qualifier("customRedisTemplate") RedisTemplate<String, String> redisTemplate
    ) {
        this.repo = repo;
        this.aes = aes;
        this.revokeService = revokeService;
        this.redisTemplate = redisTemplate;
        this.securityProperties = securityProperties;
    }

    @Override
    public String create(UUID userId, String deviceId, String rawRefreshTokenFromCognito) {
        return createAndPersist(userId, deviceId, rawRefreshTokenFromCognito);
    }

    private String createAndPersist(UUID userId, String deviceId, String raw) {
        String encrypted = aes.encrypt(raw);

        UserToken entity = new UserToken();
        entity.setUserId(userId);
        entity.setRefreshTokenEncrypted(encrypted);
        entity.setIssuedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusDays(7));
        entity.setDeviceId(deviceId);
        entity.setStatus("ACTIVE");

        repo.save(entity);

        try {
            String redisKey = redisPrefix + "active:" + HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), raw);
            redisTemplate.opsForValue().set(redisKey, entity.getId().toString(), redisTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.warn("Failed to set active refresh token in redis: {}", ex.getMessage());
        }

        return raw;
    }

    @Override
    public boolean validate(String rawToken) {
        if (revokeService.isRevoked(rawToken)) return false;
        try {
            String redisKey = redisPrefix + "active:" + rawToken;
            String val = redisTemplate.opsForValue().get(redisKey);
            if (val != null) {
                UUID tokenId = UUID.fromString(val);
                return repo.findById(tokenId)
                        .filter(t -> t.getStatus().equals("ACTIVE") && t.getExpiresAt().isAfter(LocalDateTime.now()))
                        .isPresent();
            }
        } catch (Exception ex) {
            log.debug("redis check failed: {}", ex.getMessage());
        }
        List<UserToken> candidates = repo.findActiveNotExpired(LocalDateTime.now());
        for (UserToken t : candidates) {
            try {
                String decrypted = aes.decrypt(t.getRefreshTokenEncrypted());
                if (rawToken.equals(decrypted)) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    @Override
    public UUID getUserId(String rawToken) {
        try {
            String redisKey = redisPrefix + "active:" + rawToken;
            String idStr = redisTemplate.opsForValue().get(redisKey);
            if (idStr != null) {
                UUID tokenId = UUID.fromString(idStr);
                return repo.findById(tokenId)
                        .map(UserToken::getUserId)
                        .orElseThrow(() -> new RuntimeException("Invalid token"));
            }
        } catch (Exception ex) {
            log.debug("redis get failed: {}", ex.getMessage());
        }

        List<UserToken> candidates = repo.findActiveNotExpired(LocalDateTime.now());
        for (UserToken t : candidates) {
            try {
                String decrypted = aes.decrypt(t.getRefreshTokenEncrypted());
                if (rawToken.equals(decrypted)) {
                    return t.getUserId();
                }
            } catch (Exception e) {
            }
        }

        throw new RuntimeException("Invalid token");
    }

    @Override
    public void revoke(String rawToken) {
        List<UserToken> candidates = repo.findActiveNotExpired(LocalDateTime.now());
        for (UserToken t : candidates) {
            try {
                String decrypted = aes.decrypt(t.getRefreshTokenEncrypted());
                if (rawToken.equals(decrypted)) {
                    t.setStatus("REVOKED");
                    repo.save(t);
                    break;
                }
            } catch (Exception e) {}
        }

        try {
            String redisKey = redisPrefix + "active:" + HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), rawToken);
            redisTemplate.delete(redisKey);
        } catch (Exception ex) {
            log.debug("failed deleting active redis key: {}", ex.getMessage());
        }
    }
}
