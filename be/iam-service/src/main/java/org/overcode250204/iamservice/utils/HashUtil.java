package org.overcode250204.iamservice.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HashUtil {

    public static String hmacSha256Base64(String secretBase64Pepper, String input) {
        try {
            byte[] pepper = Base64.getDecoder().decode(secretBase64Pepper);
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
