package org.overcode250204.iamservice.services.auth.impls;


import lombok.RequiredArgsConstructor;
import org.overcode250204.iamservice.services.auth.TokenRevokeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenRevokeServiceImpl implements TokenRevokeService {

    private final RedisTemplate<String, String> redisTemplate;
    @Value("${REDIS_TOKEN_PREFIX:refresh-token:}")
    private String prefix;

    public TokenRevokeServiceImpl(@Qualifier("customRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String key(String refreshToken) {
        return prefix + refreshToken;
    }

    public boolean isRevoked(String refreshToken) {
        return redisTemplate.hasKey(key(refreshToken));
    }
}
