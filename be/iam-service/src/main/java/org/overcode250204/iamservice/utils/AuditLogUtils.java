package org.overcode250204.iamservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.iamservice.entities.OutboxEvent;
import org.overcode250204.iamservice.exceptions.ErrorCode;
import org.overcode250204.iamservice.exceptions.IamServiceException;
import org.overcode250204.iamservice.repositories.OutboxEventRepository;
import org.overcode250204.iamservice.services.crypto.AESEncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogUtils {

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper;

    private final AESEncryptionService aesService;

    @Value("${spring.application.name}")
    private String serviceName;

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


            outboxEventRepository.save(
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
            throw new IamServiceException(ErrorCode.FAIL_TO_CREATE_AUDIT_LOGS_SEND_TO_MONITORING_SERVICE);
        }
    }

    public  <T> void updateEncryptedField(
            Supplier<String> encryptedGetter,
            T newValue,
            Consumer<String> encryptedSetter,
            String fieldName,
            List<String> changesList,
            boolean isPII
    ) {
        if (newValue == null) {
            return;
        }

        String oldValue;
        try {
            oldValue = aesService.decrypt(encryptedGetter.get());
        } catch (Exception e) {
            oldValue = null;
        }

        if (!Objects.equals(oldValue, newValue.toString())) {
            encryptedSetter.accept(aesService.encrypt(newValue.toString()));

            String logMessage = String.format(
                    "Field '%s' was updated (from: '%s' to: '%s')",
                    fieldName,
                    MaskData.maskData(oldValue, isPII),
                    MaskData.maskData(newValue, isPII)
            );
            changesList.add(logMessage);
        }
    }






}


