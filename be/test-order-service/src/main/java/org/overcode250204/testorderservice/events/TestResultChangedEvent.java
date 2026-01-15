package org.overcode250204.testorderservice.events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class TestResultChangedEvent extends ApplicationEvent {
    private final UUID testOrderId;

    public TestResultChangedEvent(Object source, UUID testOrderId) {
        super(source);
        this.testOrderId = testOrderId;
    }

    public UUID getTestOrderId() {
        return testOrderId;
    }
}
