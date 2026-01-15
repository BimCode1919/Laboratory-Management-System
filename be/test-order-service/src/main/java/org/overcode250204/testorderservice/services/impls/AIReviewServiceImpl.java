package org.overcode250204.testorderservice.services.impls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.dtos.AiReviewResponseDTO;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.overcode250204.testorderservice.services.AIReviewService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIReviewServiceImpl implements AIReviewService {

    private final Client geminiClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.model}")
    private String modelName;

    private static final Schema RESPONSE_SCHEMA = Schema.builder()
            .type(Type.Known.OBJECT)
            .description("AI review response schema")
            .properties(
                    Map.of(
                            "ai_has_issue",
                            Schema.builder()
                                    .type(Type.Known.BOOLEAN)
                                    .description("TRUE if system QA issue found, FALSE otherwise.")
                                    .build(),
                            "ai_review_comment",
                            Schema.builder()
                                    .type(Type.Known.STRING)
                                    .description("Concise clinical interpretation or QA explanation.")
                                    .build()
                    ))
            .required(List.of("ai_has_issue", "ai_review_comment"))
            .build();

    @Override
    public List<TestResults> generateReview(List<TestResults> results) {
        if (results == null || results.isEmpty()) return results;

        String patientNotes = results.getFirst().getTestOrder() != null
                ? results.getFirst().getTestOrder().getNotes()
                : "None provided.";

        String resultsJson;
        try {
            List<Object> dataForAI = results.stream()
                    .map(r -> objectMapper.createObjectNode()
                            .put("parameter", r.getParameterName())
                            .put("value", r.getResultValue())
                            .put("unit", r.getUnit())
                            .put("ref_low", r.getReferenceLow())
                            .put("ref_high", r.getReferenceHigh())
                            .put("flag", Objects.toString(r.getAlertLevel(), "NONE")))
                    .collect(Collectors.toList());

            resultsJson = objectMapper.writeValueAsString(dataForAI);
        } catch (Exception e) {
            log.error("JSON serialization failed for AI context: ", e);
            return results;
        }

        String promptBase = createPrompt(patientNotes, resultsJson);

        for (TestResults result : results) {
            String alertLevelName = Objects.toString(result.getAlertLevel(), "NONE");

            if (alertLevelName.equals("NORMAL") || alertLevelName.equals("NONE")) {
                result.setAiReviewComment("Result is within expected limits (Automated Review).");
                result.setAiHasIssue(false);
                continue;
            }

            String currentResultDetails = String.format(
                    "\n--- RESULT TO FOCUS ON ---\nParameter: %s, Value: %f, Flag: %s",
                    result.getParameterName(), result.getResultValue(), alertLevelName
            );

            String prompt = promptBase + currentResultDetails;
            Content content = Content.fromParts(Part.fromText(prompt));

            try {
                GenerateContentResponse response = geminiClient.models.generateContent(
                        modelName,
                        List.of(content),
                        GenerateContentConfig.builder()
                                .responseSchema(RESPONSE_SCHEMA)
                                .responseMimeType("application/json")
                                .build()
                );

                String aiResponseText = response.text();

                AiReviewResponseDTO aiReview = objectMapper.readValue(aiResponseText, AiReviewResponseDTO.class);

                result.setAiReviewComment(aiReview.getAi_review_comment());
                result.setAiHasIssue(aiReview.getAi_has_issue());

            } catch (Exception e) {
                log.error("AI Review failed for result {} (API call or JSON parse error): ", result.getParameterName(), e);

                result.setAiReviewComment("Automated clinical review failed due to system/API error.");
                result.setAiHasIssue(false);
            }
        }

        return results;
    }

    private String createPrompt(String patientNotes, String resultsJson) {
        return """
                You are a clinical laboratory QA reviewer.
                Review the flagged test result in context of all results and patient notes.
                Reply ONLY with a single JSON object matching this schema:
                {
                  "ai_has_issue": boolean,
                  "ai_review_comment": string
                }
                
                Rules:
                1. Write an extremely short, concise, and easy-to-understand clinical interpretation for the flagged result.
                2. Set ai_has_issue = true only if the value seems erroneous or the flag looks wrong.
                
                Patient notes: "%s"
                Full results JSON: %s
                """.formatted(patientNotes, resultsJson);
    }
}
