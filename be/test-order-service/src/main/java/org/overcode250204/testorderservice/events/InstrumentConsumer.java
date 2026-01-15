package org.overcode250204.testorderservice.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.dtos.HL7TestResult;
import org.overcode250204.testorderservice.exceptions.ErrorCode;
import org.overcode250204.testorderservice.exceptions.HL7ParsingException;
import org.overcode250204.testorderservice.exceptions.TestOrderException;
import org.overcode250204.testorderservice.models.entites.InboxEvent;
import org.overcode250204.testorderservice.models.entites.TestResultRaw;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.overcode250204.testorderservice.repositories.InboxRepository;
import org.overcode250204.testorderservice.services.HL7ParserService;
import org.overcode250204.testorderservice.services.TestResultProcessingService;
import org.overcode250204.testorderservice.services.TestResultRawService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstrumentConsumer {

    private final HL7ParserService hl7ParserService;
    private final TestResultRawService testResultRawService;
    private final ObjectMapper objectMapper;

    private final TestResultProcessingService testResultProcessingService;

    private final InboxRepository inboxRepository;

    @KafkaListener(
            topics = "${app.kafka.topics.hl7.testResult}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void onRawTestResult(Map<String, Object> message) {
            try {
                String eventId = (String) message.get("eventId");
                Map<String, Object> payload = (Map<String, Object>) message.get("payload");


                UUID eventIdUUID = UUID.fromString(eventId);

                if (inboxRepository.existsByEventId(eventIdUUID)) {
                    log.info("Test Order already prcessed for eventId = {}", eventId);
                    return;
                }


                String data = objectMapper.writeValueAsString(message);
                InboxEvent inboxEvent = new InboxEvent();
                inboxEvent.setEventId(eventIdUUID);
                inboxEvent.setPayload(data);
                inboxEvent.setProcessedAt(Instant.now());
                inboxRepository.save(inboxEvent);


                String hl7Message = (String) payload.get("hl7Message");
                if (hl7Message == null) {
                    throw new TestOrderException(ErrorCode.HL7_PARSING_FAILED);
                }
                HL7TestResult parsedResult = hl7ParserService.parseHL7Message(hl7Message);

                List<TestResultRaw> savedTestResults = testResultRawService.saveRawTestResults(payload, parsedResult);
                log.info("Saved {} raw results for eventId={}", savedTestResults.size(), eventIdUUID);
            } catch (HL7ParsingException ex) {
                throw new TestOrderException(ErrorCode.HL7_PARSING_FAILED);
            } catch (Exception e) {
                throw new TestOrderException(ErrorCode.FAIL_TO_LISTEN_HL7_TEST_RESULT_PUBLISH);
            }

        }
    @KafkaListener(topics = "${app.kafka.topics.instrument.runCompleted}", groupId = "${spring.kafka.consumer.group-id}")
    @RetryableTopic(attempts = "5")
    @Transactional
    public void onInstrumentRunCompleted(Map<String, Object> payload) {
        try {

            Map<String, Object> data = (Map<String, Object>) payload.get("payload");
            String eventId = (String) payload.get("eventId");
            String runId = (String) data.get("runId");
            UUID runIdUUID = UUID.fromString(runId);


            if (inboxRepository.existsByEventId(UUID.fromString(eventId))) {
                log.info("Message already prcessed for eventId = {}", eventId);
                return;
            }
            String dataJson = objectMapper.writeValueAsString(data);
            InboxEvent inboxEvent = new InboxEvent();
            inboxEvent.setEventId(UUID.fromString(eventId));
            inboxEvent.setProcessedAt(Instant.now());
            inboxEvent.setPayload(dataJson);
            inboxRepository.save(inboxEvent);



            List<TestResults> processed = testResultProcessingService.processRawResults(List.of(runIdUUID));


            log.info("[Processor] Finished processing for runId={}. Total normalized: {}", runIdUUID, processed.size());
        } catch (Exception e) {
            throw new TestOrderException(ErrorCode.ERROR_TO_PROCESS_RAW_RESULT_MESSAGE_FROM_INSTRUMENT);
        }





    }
}
