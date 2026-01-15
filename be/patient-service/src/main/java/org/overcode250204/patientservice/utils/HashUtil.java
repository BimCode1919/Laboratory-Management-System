package org.overcode250204.patientservice.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class HashUtil {

    @Value("${security.pepper.base64}")
    private String base64;

    public String hmacSha256Base64(String input) {
        try {
            byte[] pepper = Base64.getDecoder().decode(base64);
            SecretKeySpec key = new SecretKeySpec(pepper, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] raw = mac.doFinal(input.trim().toLowerCase().getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
