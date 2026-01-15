package org.overcode250204.iamservice.utils;

import lombok.RequiredArgsConstructor;
import org.overcode250204.iamservice.configs.cognito.CognitoProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CognitoUtils {

    private final CognitoProperties props;

    public String calculateSecretHash(String username) {
        try {
            String message = username + props.getClientId();
            SecretKeySpec keySpec = new SecretKeySpec(
                    props.getClientSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            return Base64.getEncoder().encodeToString(
                    mac.doFinal(message.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new RuntimeException("Error generating secret hash", e);
        }
    }
}
