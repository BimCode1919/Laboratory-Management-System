package org.overcode250204.patientservice.configs.aesgcm;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SecurityProperties {
    @Value("${security.pepper.base64}")
    private String base64Pepper;

    @Value("${security.aes.secret-key-base64}")
    private String secretKeyBase64;
}
