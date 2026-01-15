package org.overcode250204.instrumentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "inbox_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID eventId;

    @Column(name = "payload", columnDefinition = "text")
    private String payload;

    private Instant processedAt;
}
