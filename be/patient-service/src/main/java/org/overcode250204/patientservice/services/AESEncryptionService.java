package org.overcode250204.patientservice.services;

public interface AESEncryptionService {
    String encrypt(String plaintext);
    String decrypt(String base64Ciphertext);
}
