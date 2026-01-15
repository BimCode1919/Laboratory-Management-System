package org.overcode250204.monitoringservice.entities;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.UUID;

@Document(collection = "inbox_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InboxEvent {

    @Id
    private String id;
    /**
     * Đảm bảo không thể có 2 sự kiện (event) với cùng một eventId
     * Điều này thực thi Idempotency (xử lý 1 lần duy nhất) ở cấp độ CSDL
     */
    @Indexed(unique = true)
    private UUID eventId;

    private String payload;

    @CreatedDate
    private Instant processedAt;
}