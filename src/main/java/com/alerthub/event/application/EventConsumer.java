package com.alerthub.event.application;

import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventConsumer {

    private final EventProcessingService eventProcessingService;

    public EventConsumer(EventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    @Incoming("events-in")
    @Blocking
    public void onMessage(String eventId) {
        eventProcessingService.process(UUID.fromString(eventId));
    }
}
