package org.overcode250204.iamservice.services.crypto;

public interface AESEncryptionService {
    String encrypt(String plaintext);
    String decrypt(String base64Ciphertext);
}
