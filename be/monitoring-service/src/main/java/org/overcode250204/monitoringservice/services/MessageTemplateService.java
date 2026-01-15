package org.overcode250204.monitoringservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.monitoringservice.entities.EventTemplate;
import org.overcode250204.monitoringservice.repositories.EventTemplateRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTemplateService {

    private final EventTemplateRepo templateRepository;

    @Value("${app.monitoring.autoCreateTemplate:false}")
    private boolean autoCreateTemplate;


    public String renderMessage(String eventName, Map<String, Object> payload) {
        EventTemplate templateEntity = templateRepository.findByEventName(eventName)
                .orElseGet(() -> createTemplateIfMissing(eventName, payload));

        String message = templateEntity.getTemplate();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }

        return message.replace("{eventName}", eventName);
    }

    public String resolveSeverity(String eventName) {
        return templateRepository.findByEventName(eventName)
                .map(EventTemplate::getSeverity)
                .orElse("INFO");
    }

    private EventTemplate createTemplateIfMissing(String eventName, Map<String, Object> payload) {
        if (!autoCreateTemplate) {
            log.warn("[MessageTemplateService] Template not found for '{}', autoCreateTemplate=false", eventName);
            return EventTemplate.builder()
                    .eventName(eventName)
                    .template("Event {eventName}")
                    .severity("INFO")
                    .description("Template not found, please define manually")
                    .build();
        }

        // Create new template
        String autoTemplate = "Event " + eventName + " occurred with payload: " + payload.keySet();

        EventTemplate newTemplate = EventTemplate.builder()
                .eventName(eventName)
                .template(autoTemplate)
                .severity("INFO")
                .description("Auto-generated from runtime event (please refine later)")
                .build();

        try {
            templateRepository.save(newTemplate);
            log.info("[MessageTemplateService] Created new template for event '{}'", eventName);
        } catch (Exception e) {
            log.error("[MessageTemplateService] Failed to save new template for '{}'", eventName, e);
        }

        return newTemplate;
    }
}
