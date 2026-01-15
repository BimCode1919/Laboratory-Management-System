package org.overcode250204.instrumentservice.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MockAnalyzerAdapter {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.analysisResponse}")
    private String analysisResponseTopic;

    @KafkaListener(topics = "${app.kafka.topics.analysisRequest}",
            groupId = "mock-analyzer-consumers")
    public void onAnalysisRequest(Map<String, Object> request) {
        try {
            log.info("[MockAdapter] Received request: {}", request);

            String barcode = (String) request.get("barcode");
            String testType = (String) request.get("testType");
            String instrumentId = (String) request.get("instrumentId");
            String runId = (String) request.get("runId");

            Map<String, Object> rawData = simulateAnalyzerResult(testType, barcode);


            Map<String, Object> response = new HashMap<>();
            response.put("runId", runId);
            response.put("instrumentId", instrumentId);
            response.put("barcode", barcode);
            response.put("testType", testType);
            response.put("status", "SUCCESS");
            response.put("rawData", rawData);
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("userId", request.get("userId"));
            response.put("eventId", request.get("eventId"));
            //CHECK PLS ------
            kafkaTemplate.send(
                    analysisResponseTopic,
                    instrumentId,
                    response
            );

            log.info("[MockAdapter] Sent fake analysis response for barcode={} → {}", barcode, response);

        } catch (Exception e) {
            log.error("[MockAdapter] Error processing request: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> simulateAnalyzerResult(String testType, String barcode) {
        Map<String, Object> data = new HashMap<>();
        data.put("barcode", barcode);
        data.put("testType", testType);
        switch (testType.toUpperCase()) {
            case "CBC" -> {
                // WBC
                // Normal: 4.0 – 10.0 (10⁹/L)
                // Random range: 2.0 – 14.0
                data.put("WBC", round(2.0 + Math.random() * 12.0, 1));

                // RBC
                // Normal: 4.2 – 5.9 (10¹²/L)
                // Random range: 3.5 – 6.3
                data.put("RBC", round(3.5 + Math.random() * 2.8, 2));

                // HGB
                // Normal: 12 – 18 g/dL
                // Random range: 8.0 – 20.0
                data.put("HGB", round(8.0 + Math.random() * 12.0, 1));

                // HCT
                // Normal: 36 – 52 %
                // Random range: 28 – 58
                data.put("HCT", round(28.0 + Math.random() * 30.0, 1));

                // MCV
                // Normal: 80 – 100 fL
                // Random range: 70 – 110
                data.put("MCV", round(70.0 + Math.random() * 40.0, 1));

                // MCH
                // Normal: 27 – 34 pg
                // Random range: 22 – 37
                data.put("MCH", round(22.0 + Math.random() * 15.0, 1));

                // MCHC
                // Normal: 32 – 36 g/dL
                // Random range: 28 – 40
                data.put("MCHC", round(28.0 + Math.random() * 12.0, 1));

                // PLT
                // Normal: 150,000 – 400,000 /µL
                // Random range: 100,000 – 500,000
                data.put("PLT", (int) (100000 + Math.random() * 400000));
            }

            case "HBA1C" -> {
                // HbA1c
                // Normal: 4.0 – 5.6 %
                // Random range: 3.5 – 9.5
                data.put("HbA1C", round(3.5 + Math.random() * 6.0, 1));
            }

            case "LFT" -> {
                // ALT
                // Normal: 7 – 56 U/L
                // Random range: 5 – 120
                data.put("ALT", round(5 + Math.random() * 115, 1));

                // AST
                // Normal: 10 – 40 U/L
                // Random range: 5 – 100
                data.put("AST", round(5 + Math.random() * 95, 1));

                // ALP
                // Normal: 44 – 147 U/L
                // Random range: 30 – 220
                data.put("ALP", round(30 + Math.random() * 190, 1));

                // GGT
                // Normal: 9 – 48 U/L
                // Random range: 5 – 120
                data.put("GGT", round(5 + Math.random() * 115, 1));

                // Total Bilirubin (TBIL)
                // Normal: 0.1 – 1.2 mg/dL
                // Random range: 0.05 – 3.0
                data.put("TBIL", round(0.05 + Math.random() * 2.95, 2));

                // Direct Bilirubin (DBIL)
                // Normal: 0.0 – 0.3 mg/dL
                // Random range: 0.0 – 1.2
                data.put("DBIL", round(Math.random() * 1.2, 2));

                // Albumin
                // Normal: 3.5 – 5.0 g/dL
                // Random range: 3.0 – 6.0
                data.put("Albumin", round(3.0 + Math.random() * 3.0, 2));

                // Total Protein
                // Normal: 6.0 – 8.3 g/dL
                // Random range: 5.0 – 9.0
                data.put("TotalProtein", round(5.0 + Math.random() * 4.0, 2));
            }

            default -> data.put("RESULT", Math.random() * 100);
        }
        return data;
    }

    private double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}
