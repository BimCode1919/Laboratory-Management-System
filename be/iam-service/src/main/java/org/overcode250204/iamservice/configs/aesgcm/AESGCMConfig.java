package org.overcode250204.iamservice.configs.aesgcm;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class AESGCMConfig {

    private final SecurityProperties securityProperties;

    private SecretKey secretKey;

    @PostConstruct
    public void init(){
        byte[] decoded = Base64.getDecoder().decode(securityProperties.getSecretKeyBase64());
        log.info("Loading AES key (len={} bytes)", decoded.length);
        this.secretKey = new SecretKeySpec(decoded, 0, decoded.length, "AES");
    }

    @Bean
    public SecretKey aesSecretKey(){
        return secretKey;
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
