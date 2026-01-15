package org.overcode250204.instrumentservice.service.interfaces;


import java.util.UUID;

public interface EventLogService {

    void logEvent(UUID instrumentId, String eventType, String message, UUID performedBy);

}
