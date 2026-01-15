package org.overcode250204.iamservice.configs.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private RedisProperties redis = new RedisProperties();

    @Getter
    @Setter
    public static class RedisProperties {
        private TokenProperties token = new TokenProperties();

        @Getter
        @Setter
        public static class TokenProperties {
            private String prefix;
            private Long ttl;
        }
    }
}
