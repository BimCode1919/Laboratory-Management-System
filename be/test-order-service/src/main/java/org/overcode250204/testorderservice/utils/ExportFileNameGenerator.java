package org.overcode250204.testorderservice.utils;

import org.overcode250204.testorderservice.models.enums.ExportFileType;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ExportFileNameGenerator {

    public static String generateExportFileName(UUID testOrderId, String patientName, LocalDateTime timestamp, ExportFileType type) {
        String formattedPatientName = sanitizeName(patientName);
        return String.format("testorder_%s_%s_%s.%s",
                testOrderId,
                formattedPatientName,
                timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                type.name().toLowerCase());
    }

    private static String sanitizeName(String input){
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFKC);
        return normalized.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
