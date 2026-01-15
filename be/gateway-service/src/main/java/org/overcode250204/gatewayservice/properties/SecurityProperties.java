package org.overcode250204.gatewayservice.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "security.cognito")
public class SecurityProperties {
    private String jwksUrl;

    private String issuer;

    private String audience;

    private String clientId;

    private int jwtRefreshIntervalMins;

}
