package com.alerthub.event.application;

import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventPublisher {

    @Inject
    @Channel("events-out")
    Emitter<String> emitter;

    public void publish(UUID eventId) {
        emitter.send(eventId.toString());
    }
}
