package org.overcode250204.iamservice.services.crypto.impl;

import lombok.RequiredArgsConstructor;
import org.overcode250204.iamservice.exceptions.IamServiceException;
import org.overcode250204.iamservice.exceptions.ErrorCode;
import org.overcode250204.iamservice.services.crypto.AESEncryptionService;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AESEncryptionServiceImpl implements AESEncryptionService {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_BIT_LENGHT = 128;

    private final SecretKey aesSecretKey;
    private final SecureRandom secureRandom;

    @Override
    public String encrypt(String plaintext){
        if(plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGHT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesSecretKey, spec);

            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ct, 0, combined, iv.length, ct.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IamServiceException(ErrorCode.AES_ENCRYPT_FAILED);
        }
    }

    @Override
    public String decrypt(String base64Ciphertext){
        if (base64Ciphertext == null) return null;
        try {
            byte[] all = Base64.getDecoder().decode(base64Ciphertext);
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(all, 0, iv, 0, IV_SIZE);
            byte[] ct = new byte[all.length - IV_SIZE];
            System.arraycopy(all, IV_SIZE, ct, 0, ct.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGHT, iv);
            cipher.init(Cipher.DECRYPT_MODE, aesSecretKey, spec);

            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e){
            throw new IamServiceException(ErrorCode.AES_DECRYPT_FAILED);
        }
    }
}
