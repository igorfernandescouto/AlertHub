package com.alerthub.event.application;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;

public record CreateEventCommand(
        String eventType,
        String source,
        JsonNode payload,
        Instant occurredAt,
        String idempotencyKey
) {
}
