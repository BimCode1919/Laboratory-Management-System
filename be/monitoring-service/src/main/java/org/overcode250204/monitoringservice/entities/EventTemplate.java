package org.overcode250204.monitoringservice.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("event_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTemplate {

    @Id
    private String id;

    private String eventName;

    private String template;

    private String severity;

    private String description;
}
