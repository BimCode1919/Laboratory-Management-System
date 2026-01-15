package org.overcode250204.patientservice.configs.aesgcm;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.overcode250204.patientservice.services.AESEncryptionService;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = false)
@RequiredArgsConstructor
public class EncryptionConverter implements AttributeConverter<String, String> {

    private final AESEncryptionService aesEncryptionService;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return aesEncryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return aesEncryptionService.decrypt(dbData);
    }
}
