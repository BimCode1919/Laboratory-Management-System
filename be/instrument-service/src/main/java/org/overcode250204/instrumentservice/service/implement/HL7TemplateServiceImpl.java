package org.overcode250204.instrumentservice.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import org.overcode250204.instrumentservice.entity.Instrument;
import org.overcode250204.instrumentservice.service.interfaces.HL7TemplateService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Service
public class HL7TemplateServiceImpl implements HL7TemplateService {

    private static final DateTimeFormatter HL7_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String buildOruMessage(Instrument instrument, String barcode, String testType, JsonNode rawData) {
        StringBuilder hl7 = new StringBuilder();

        hl7.append(buildMSH(instrument));
        hl7.append(String.format("PID|||%s||\n", barcode));
        hl7.append(buildORC());
        hl7.append(buildOBR(testType));
        hl7.append(buildOBXSegments(rawData));

        return hl7.toString();
    }

    //SEGMENT BUILDERS

    private String buildMSH(Instrument instrument) {
        String now = LocalDateTime.now().format(HL7_DATE_FORMAT);
        return String.format(
                "MSH|^~\\&|%s|%s|LIS|LAB|%s||ORU^R01|%s|P|2.3.1\n",
                instrument.getInstrumentCode(),
                instrument.getName(),
                now,
                UUID.randomUUID()
        );
    }

    private String buildORC() {
        String now = LocalDateTime.now().format(HL7_DATE_FORMAT);
        return String.format("ORC|RE||%s||CM|||||%s|\n", UUID.randomUUID(), now);
    }

    private String buildOBR(String testType) {
        String description = switch (testType.toUpperCase()) {
            case "CBC" -> "Complete Blood Count";
            case "HBA1C" -> "Glycated Hemoglobin (HbA1C)";
            case "LFT" -> "Liver Function Test";
            case "RFT" -> "Renal Function Test";
            default -> "General Laboratory Test";
        };

        String now = LocalDateTime.now().format(HL7_DATE_FORMAT);
        return String.format("OBR|1|||%s^%s||%s||||||\n", testType, description, now);
    }

    private String buildOBXSegments(JsonNode rawData) {
        StringBuilder obx = new StringBuilder();
        int i = 1;

        for (Iterator<Map.Entry<String, JsonNode>> it = rawData.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();

            String param = entry.getKey();
            String value = entry.getValue().asText();

            if (param.equalsIgnoreCase("barcode") || param.equalsIgnoreCase("testType"))
                continue;

            String unit = guessUnit(param);

            obx.append(String.format("OBX|%d|NM|%s^%s||%s|%s|||F|\n",
                    i++, param, param, value, unit));
        }

        return obx.toString();
    }

    private String guessUnit(String param) {
        return switch (param.toUpperCase()) {

            // ===== CBC =====
            case "WBC" -> "10^9/L";
            case "RBC" -> "10^12/L";
            case "HGB" -> "g/dL";
            case "HCT" -> "%";
            case "MCV" -> "fL";
            case "MCH" -> "pg";
            case "MCHC" -> "g/dL";
            case "PLT" -> "10^9/L";

            // ===== HbA1C =====
            case "HBA1C" -> "%";   // HbA1c reported in %

            // ===== LFT =====
            case "ALT" -> "U/L";           // Alanine Aminotransferase
            case "AST" -> "U/L";           // Aspartate Aminotransferase
            case "ALP" -> "U/L";           // Alkaline Phosphatase
            case "GGT" -> "U/L";           // Gamma-glutamyl transferase
            case "TBIL" -> "mg/dL";        // Total Bilirubin
            case "DBIL" -> "mg/dL";        // Direct Bilirubin
            case "ALBUMIN" -> "g/dL";      // Albumin
            case "TOTALPROTEIN" -> "g/dL"; // Total Protein

            default -> "";
        };
    }

}
