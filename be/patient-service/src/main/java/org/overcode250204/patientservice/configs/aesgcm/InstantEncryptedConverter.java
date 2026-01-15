package org.overcode250204.patientservice.configs.aesgcm;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.overcode250204.patientservice.services.AESEncryptionService;

import java.time.Instant;

@Converter(autoApply = false)
@RequiredArgsConstructor
public class InstantEncryptedConverter implements AttributeConverter<Instant, String> {

    private final AESEncryptionService aesEncryptionService;

    @Override
    public String convertToDatabaseColumn(Instant attribute) {
        if (attribute == null) return null;
        return aesEncryptionService.encrypt(attribute.toString());
    }

    @Override
    public Instant convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return Instant.parse(aesEncryptionService.decrypt(dbData));
    }
}
