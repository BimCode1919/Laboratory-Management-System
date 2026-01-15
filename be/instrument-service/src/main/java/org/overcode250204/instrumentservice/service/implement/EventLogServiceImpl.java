package org.overcode250204.instrumentservice.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.entity.InstrumentEventLog;
import org.overcode250204.instrumentservice.repository.InstrumentEventLogRepository;
import org.overcode250204.instrumentservice.service.interfaces.EventLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogServiceImpl implements EventLogService {

    private final InstrumentEventLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void logEvent(UUID instrumentId, String eventType, String message, UUID performedBy) {
        try {
            InstrumentEventLog logEntry = new InstrumentEventLog();
            logEntry.setInstrumentId(instrumentId);
            logEntry.setEventType(eventType);
            logEntry.setPerformedBy(performedBy);
            logEntry.setTimestamp(LocalDateTime.now());

            JsonNode details = objectMapper.createObjectNode().put("message", message);
            logEntry.setDetails(details);

            eventLogRepository.save(logEntry);
            log.info("Logged event [{}] for instrument {}: {}", eventType, instrumentId, message);

        } catch (Exception e) {
            log.error("Failed to log event {} for instrument {}", eventType, instrumentId, e);
        }
    }

}
