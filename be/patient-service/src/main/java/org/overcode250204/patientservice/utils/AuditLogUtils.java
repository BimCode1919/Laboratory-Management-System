package org.overcode250204.patientservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.patientservice.entities.OutboxEvent;
import org.overcode250204.patientservice.exceptions.ErrorCode;
import org.overcode250204.patientservice.exceptions.PatientException;
import org.overcode250204.patientservice.repositories.OutboxRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogUtils {

    private final ObjectMapper objectMapper;

    private final OutboxRepository outboxRepository;

    private static final Pattern EMAIL_REGEX = Pattern.compile("(?<=.).(?=.*@)");
    private static final Pattern PHONE_REGEX = Pattern.compile("\\d(?=\\d{4})");

    public void createAuditOutboxEvent(
            String aggregateType,
            String aggregateId,
            String eventType,
            String userIdAction,
            Map<String, Object> payload
    ) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.putAll(payload);
            data.put("aggregateType", aggregateType);
            data.put("aggregateId", aggregateId);
            data.put("eventType", eventType);
            data.put("performedBy", userIdAction);


            outboxRepository.save(
                    OutboxEvent.builder()
                            .aggregateType("IAM_MONITORING")
                            .aggregateId(aggregateId)
                            .eventType(eventType)
                            .payload(objectMapper.writeValueAsString(data))
                            .createdAt(Instant.now())
                            .status("PENDING")
                            .build()
            );

        } catch (Exception e) {
            log.error("CRITICAL: Failed to create audit outbox event for aggregate {}. Rolling back.", aggregateId, e);
            throw new PatientException(ErrorCode.FAIL_TO_CREATE_AUDIT_LOGS_SEND_TO_MONITORING_SERVICE);
        }
    }

    public <T> void updateField(T oldValue, T newValue, Consumer<T> setter, String fieldName, List<String> changes) {
        if (newValue != null && !Objects.equals(oldValue, newValue)) {
            setter.accept(newValue);
            String logMessage = String.format("Field '%s' was updated (from: '%s' to '%s')", fieldName, maskData(oldValue), maskData(newValue));
            changes.add(logMessage);
        }
    }

    private String maskData(Object data) {
        if (data == null) {
            return "<NULL>";
        }

        String s = data.toString();

        if (s.isBlank()) {
            return "<BLANK>";
        }

        boolean isSensitivePII = (s.contains("@") || s.matches(".*\\d{7,}.*"));

        if (isSensitivePII) {

            if (s.contains("@")) {
                return EMAIL_REGEX.matcher(s).replaceAll("*");
            }

            else {
                return PHONE_REGEX.matcher(s).replaceAll("*");
            }
        }


        if (s.length() > 50) {
            return s.substring(0, 47) + "...";
        }

        return s;
    }
}
